package net.explorviz.adapter.kafka;

import com.google.common.io.BaseEncoding;
import com.google.protobuf.InvalidProtocolBufferException;
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient;
import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import io.opencensus.proto.dump.DumpSpans;
import io.opencensus.proto.trace.v1.AttributeValue;
import io.opencensus.proto.trace.v1.Span;
import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Map;
import java.util.Properties;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import net.explorviz.adapter.translation.SpanConverter;
import net.explorviz.adapter.validation.InvalidSpanException;
import net.explorviz.adapter.validation.SpanSanitizer;
import net.explorviz.adapter.validation.SpanValidator;
import net.explorviz.avro.EVSpan;
import net.explorviz.avro.Timestamp;
import org.apache.avro.specific.SpecificRecord;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Produced;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class SpanTranslatorStream {

  private static final Logger LOGGER = LoggerFactory.getLogger(SpanTranslatorStream.class);

  private final SchemaRegistryClient registry;

  private final KafkaConfig config;

  private final Properties streamsConfig = new Properties();

  private Topology topology;

  private SpanValidator validator;
  private SpanSanitizer sanitizer;

  private SpanConverter converter;

  private KafkaStreams streams;

  @Inject
  public SpanTranslatorStream(final SchemaRegistryClient registry, final KafkaConfig config,
                              SpanConverter converter, final SpanValidator validator,
                              final SpanSanitizer sanitizer) {
    this.registry = registry;
    this.config = config;

    this.converter = converter;
    this.validator = validator;
    this.sanitizer = sanitizer;


    this.setupStreamsConfig();
    this.buildTopology();
  }

  void onStart(@Observes final StartupEvent event) {
    this.streams = new KafkaStreams(this.topology, this.streamsConfig);
    this.streams.cleanUp();
    this.streams.start();
  }

  void onStop(@Observes final ShutdownEvent event) {
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
        return DumpSpans.parseFrom(d).getSpansList();
      } catch (InvalidProtocolBufferException e) {
        return new ArrayList<>();
      }
    });

    final KStream<String, EVSpan> traceIdEVSpanStream = spanKStream.map(($, s) -> {
      EVSpan span = converter.toEVSpan(s);
      return new KeyValue<>(span.getTraceId(), span);
    }).mapValues(s -> sanitizer.sanitize(s));

    final KStream<String, EVSpan> validEVSpanStream =
        traceIdEVSpanStream.filter(($, v) -> validator.isValid(v));
    final KStream<String, EVSpan> invalidEVSpanStream =
        traceIdEVSpanStream.filterNot(($, v) -> validator.isValid(v));


    //validEVSpanStream.to(this.config.getOutTopic(),
    //    Produced.with(Serdes.String(), this.getValueSerde()));

    validEVSpanStream.to(this.config.getOutTopic());

    // TODO: invalidEVSpanStream to Event Messages
    invalidEVSpanStream.mapValues(s -> {
      try {
        validator.validate(s);
        return null;
      } catch (InvalidSpanException e) {
        return e;
      }
    }).foreach((k, e) -> LOGGER.warn("Rejected a span {}: {}", e.getSpan(), e.getMessage()));


    this.topology = builder.build();
  }



  public Topology getTopology() {
    return this.topology;
  }

  private <T extends SpecificRecord> SpecificAvroSerde<T> getValueSerde() {
    final SpecificAvroSerde<T> valueSerde = new SpecificAvroSerde<>(this.registry);
    valueSerde.configure(
        Map.of(AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, "http://localhost:8081"),
        false);
    return valueSerde;
  }

}

