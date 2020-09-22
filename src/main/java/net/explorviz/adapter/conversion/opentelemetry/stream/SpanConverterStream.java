package net.explorviz.adapter.conversion.opentelemetry.stream;

import com.google.protobuf.InvalidProtocolBufferException;
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient;
import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import io.opentelemetry.proto.trace.v1.Span;
import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;
import java.util.Map;
import java.util.Properties;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import net.explorviz.adapter.injection.KafkaConfig;
import net.explorviz.adapter.validation.SpanValidator;
import net.explorviz.avro.SpanDynamic;
import net.explorviz.avro.SpanStructure;
import org.apache.avro.specific.SpecificRecord;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Produced;

@ApplicationScoped
public class SpanConverterStream {



  private final SchemaRegistryClient registry;

  private final KafkaConfig config;

  private final Properties streamsConfig = new Properties();

  private Topology topology;


  private final SpanValidator validator;
  private final StructureTransformer structureTransformer;
  private final DynamicTransformer dynamicTransformer;

  private KafkaStreams streams;

  @Inject
  public SpanConverterStream(final SchemaRegistryClient registry, final KafkaConfig config,
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

    final KStream<byte[], byte[]> dumpSpanStream = builder.stream(this.config.getInTopicOpenTelemetry(),
        Consumed.with(Serdes.ByteArray(), Serdes.ByteArray()));

    final KStream<byte[], Span> spanKStream = dumpSpanStream.mapValues(v -> {
      try {
        return Span.parseFrom(v);
      } catch (InvalidProtocolBufferException e) {
        e.printStackTrace();
        return null;
      }
    });

    spanKStream.foreach((k,v) -> System.out.println("SPANID: "+ v.getSpanId()));
    spanKStream.foreach((k,v) -> System.out.println("TRACEID: "+ v.getTraceId() +  "("+v.getTraceId().toStringUtf8()+")"));

    KStream<String, SpanStructure> spanStructureStream =
        spanKStream.transform(() -> structureTransformer);

    final KStream<String, SpanStructure> validSpanStructureStream =
        spanStructureStream.filter(($, v) -> this.validator.isValid(v));
    // final KStream<String, SpanStructure> invalidSpanStructureStream =
    // spanStructureStream.filterNot(($, v) -> this.validator.isValid(v));


    KStream<String, SpanDynamic> spanDynamicStream =
        spanKStream.transform(() -> dynamicTransformer);


    validSpanStructureStream
        .to(this.config.getStructureOutTopic(),
            Produced.with(Serdes.String(), this.getValueSerde()));

    spanDynamicStream.to(this.config.getDynamicOutTopic(),
        Produced.with(Serdes.String(), this.getValueSerde()));



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
