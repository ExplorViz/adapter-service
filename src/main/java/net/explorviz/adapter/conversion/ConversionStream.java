package net.explorviz.adapter.conversion;

import com.google.protobuf.InvalidProtocolBufferException;
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient;
import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import io.opencensus.proto.dump.DumpSpans;
import io.opencensus.proto.trace.v1.Span;
import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import net.explorviz.adapter.conversion.transformer.DynamicTransformer;
import net.explorviz.adapter.conversion.transformer.StructureTransformer;
import net.explorviz.adapter.injection.KafkaConfig;
import net.explorviz.adapter.service.validation.SpanValidator;
import net.explorviz.avro.SpanDynamic;
import net.explorviz.avro.SpanStructure;
import org.apache.avro.specific.SpecificRecord;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KafkaStreams.State;
import org.apache.kafka.streams.KafkaStreams.StateListener;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Produced;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Builds KafkaStreams instance with all its transformers. Entry point of the stream analysis.
 */
@ApplicationScoped
public class ConversionStream {

  private static final Logger LOGGER = LoggerFactory.getLogger(ConversionStream.class);

  private final SchemaRegistryClient registry;

  private final KafkaConfig config;

  private final Properties streamsConfig = new Properties();

  private Topology topology;

  private final SpanValidator validator;
  private final StructureTransformer structureTransformer;
  private final DynamicTransformer dynamicTransformer;

  private boolean logInitData = true;

  private KafkaStreams streams;

  @Inject
  public ConversionStream(final SchemaRegistryClient registry, final KafkaConfig config,
      final StructureTransformer structureTransformer,
      final DynamicTransformer dynamicTransformer,
      final SpanValidator validator) {
    this.registry = registry;
    this.config = config;
    this.validator = validator;
    this.structureTransformer = structureTransformer;
    this.dynamicTransformer = dynamicTransformer;

    this.setupStreamsConfig();
    this.buildTopology();
  }

  /* default */ void onStart(@Observes final StartupEvent event) { // NOPMD
    this.streams = new KafkaStreams(this.topology, this.streamsConfig);
    this.streams.cleanUp();
    this.streams.setStateListener(new ErrorStateListener());

    this.streams.start();
  }

  /* default */ void onStop(@Observes final ShutdownEvent event) { // NOPMD
    this.streams.close();
  }

  private void setupStreamsConfig() {
    this.streamsConfig.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG,
        this.config.getBootstrapServers());
    this.streamsConfig.put(StreamsConfig.APPLICATION_ID_CONFIG, this.config.getApplicationId());
  }

  private void buildTopology() {

    final StreamsBuilder builder = new StreamsBuilder();

    final KStream<byte[], byte[]> dumpSpanStream = builder.stream(this.config.getInTopic(),
        Consumed.with(Serdes.ByteArray(), Serdes.ByteArray()));

    final KStream<byte[], Span> spanKStream = dumpSpanStream.flatMapValues(d -> {
      try {
        final List<Span> spanList = DumpSpans.parseFrom(d).getSpansList();
        if (this.logInitData && LOGGER.isDebugEnabled()) {
          this.logInitData = false;
          LOGGER.debug("Received data via Kafka.");
        }

        if (LOGGER.isTraceEnabled()) {
          LOGGER.trace("Received {} spans.", spanList.size());
        }

        return spanList;
      } catch (final InvalidProtocolBufferException e) {
        return new ArrayList<>();
      }
    });

    // Validate Spans
    final KStream<byte[], Span> validSpanStream =
        spanKStream.filter((k, v) -> this.validator.isValid(v));

    // Convert to Span Structure
    final KStream<String, SpanStructure> spanStructureStream =
        validSpanStream.transform(() -> this.structureTransformer);

    // Convert to Span Dynamic
    final KStream<String, SpanDynamic> spanDynamicStream =
        validSpanStream.transform(() -> this.dynamicTransformer);

    // Forward Span Structure
    spanStructureStream
        .to(this.config.getStructureOutTopic(),
            Produced.with(Serdes.String(), this.getValueSerde()));

    // Forward Span Dynamic
    spanDynamicStream.to(this.config.getDynamicOutTopic(),
        Produced.with(Serdes.String(), this.getValueSerde()));

    this.topology = builder.build();
  }

  public Topology getTopology() {
    return this.topology;
  }

  private static class ErrorStateListener implements StateListener {

    @Override
    public void onChange(final State newState, final State oldState) {
      if (newState.equals(State.ERROR)) {

        if (LOGGER.isErrorEnabled()) {
          LOGGER.error(
              "Kafka Streams thread died. "
                  + "Are Kafka topic initialized? Quarkus application will shut down.");
        }
        Quarkus.asyncExit(-1);
      }

    }
  }

  private <T extends SpecificRecord> SpecificAvroSerde<T> getValueSerde() {
    final SpecificAvroSerde<T> valueSerde = new SpecificAvroSerde<>(this.registry);
    valueSerde.configure(
        Map.of(AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, "http://localhost:8081"),
        false);
    return valueSerde;
  }

}

