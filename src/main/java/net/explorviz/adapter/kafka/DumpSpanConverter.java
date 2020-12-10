package net.explorviz.adapter.kafka;

import com.google.protobuf.InvalidProtocolBufferException;
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient;
import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import io.opencensus.proto.dump.DumpSpans;
import io.opencensus.proto.trace.v1.Span;
import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import net.explorviz.adapter.validation.SpanValidator;
import net.explorviz.avro.SpanDynamic;
import net.explorviz.avro.SpanStructure;
import org.apache.avro.specific.SpecificRecord;

import org.apache.kafka.common.Metric;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Produced;
import org.eclipse.microprofile.metrics.Counter;
import org.eclipse.microprofile.metrics.Gauge;
import org.eclipse.microprofile.metrics.Metadata;
import org.eclipse.microprofile.metrics.MetricRegistry;
import org.eclipse.microprofile.metrics.MetricType;

@ApplicationScoped
public class DumpSpanConverter {

  @Inject
  MetricRegistry metricRegistry;

  private final SchemaRegistryClient registry;

  private final KafkaConfig config;

  private final Properties streamsConfig = new Properties();

  private Topology topology;


  private final SpanValidator validator;
  private final StructureTransformer structureTransformer;
  private final DynamicTransformer dynamicTransformer;

  private KafkaStreams streams;

  @Inject
  public DumpSpanConverter(final SchemaRegistryClient registry, final KafkaConfig config,
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
    exposeMetrics();
  }

  void onStop(@Observes final ShutdownEvent event) {
    this.streams.close();
  }

  private void setupStreamsConfig() {
    this.streamsConfig.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG,
        this.config.getBootstrapServers());
    this.streamsConfig.put(StreamsConfig.APPLICATION_ID_CONFIG, this.config.getApplicationId());
  }


  private void exposeMetrics() {
    Set<String> processed = new HashSet<>();

    for (Metric metric : streams.metrics().values()) {
      String name = metric.metricName().group() +
          ":" + metric.metricName().name();

      if (processed.contains(name)) {
        continue;
      }

      // string-typed metric not supported
      if (name.contentEquals("app-info:commit-id") ||
          name.contentEquals("app-info:version")) {
        continue;
      } else if (name.endsWith("count") || name.endsWith("total")) {
        registerCounter(metric, name);
      } else {
        registerGauge(metric, name);
      }

      processed.add(name);
    }
  }

  private void registerCounter(Metric metric, String name) {
    Metadata metadata = Metadata.builder()
      .withName(name)
      .withType(MetricType.COUNTER)
      .withDescription(metric.metricName().description())
      .build();
    metricRegistry.register(metadata, new Counter() {

      @Override
      public void inc() {}

      @Override
      public void inc(final long n) {}

      @Override
      public long getCount() {
        return (long) metric.metricValue();
      }
    } );
  }

  private void registerGauge(Metric metric, String name) {
    Metadata metadata = Metadata.builder()
        .withName(name)
        .withType(MetricType.GAUGE)
        .withDescription(metric.metricName().description())
        .build();

    metricRegistry.register(metadata, new Gauge<Double>() {
      @Override
      public Double getValue() {
        return (Double) metric.metricValue();
      }
    } );
  }

  private void buildTopology() {

    final StreamsBuilder builder = new StreamsBuilder();

    final KStream<byte[], byte[]> dumpSpanStream = builder.stream(this.config.getInTopic(),
        Consumed.with(Serdes.ByteArray(), Serdes.ByteArray()));

    final KStream<byte[], Span> spanKStream = dumpSpanStream.flatMapValues(d -> {
      try {
        return DumpSpans.parseFrom(d).getSpansList();
      } catch (final InvalidProtocolBufferException e) {
        return new ArrayList<>();
      }
    });


    KStream<String, SpanStructure> spanStructureStream =
        spanKStream.transform(() -> structureTransformer);

    spanStructureStream.foreach((k, v) ->
        System.out.println(v.toString())
    );

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

