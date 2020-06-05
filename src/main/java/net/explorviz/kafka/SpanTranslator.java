package net.explorviz.kafka;

import com.google.common.io.BaseEncoding;
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient;
import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import io.opencensus.proto.dump.DumpSpans;
import io.opencensus.proto.trace.v1.AttributeValue;
import io.opencensus.proto.trace.v1.Span;
import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
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

@ApplicationScoped
public class SpanTranslator {

  private final SchemaRegistryClient registry;

  private final KafkaConfig config;

  private final Properties streamsConfig = new Properties();

  private Topology topology;

  private KafkaStreams streams;

  @Inject
  public SpanTranslator(final SchemaRegistryClient registry, final KafkaConfig config) {
    this.registry = registry;
    this.config = config;

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

    final KStream<String, EVSpan> traceIdSpanStream = dumpSpanStream.flatMap((key, value) -> {

      DumpSpans dumpSpan;
      final List<KeyValue<String, EVSpan>> result = new LinkedList<>();
      try {

        dumpSpan = DumpSpans.parseFrom(value);

        for (final Span s : dumpSpan.getSpansList()) {
          final String traceId =
              BaseEncoding.base16().lowerCase().encode(s.getTraceId().toByteArray(), 0, 16);

          final String spanId =
              BaseEncoding.base16().lowerCase().encode(s.getSpanId().toByteArray(), 0, 8);

          final Timestamp startTime =
              new Timestamp(s.getStartTime().getSeconds(), s.getStartTime().getNanos());

          final long endTime = Instant
              .ofEpochSecond(s.getEndTime().getSeconds(), s.getEndTime().getNanos()).toEpochMilli();


          final long duration = endTime
              - Duration.ofSeconds(startTime.getSeconds(), startTime.getNanoAdjust()).toMillis();

          final Map<String, AttributeValue> attributes = s.getAttributes().getAttributeMapMap();
          final String operationName = attributes.get("method_fqn").getStringValue().getValue();
          final String hostname = attributes.get("host").getStringValue().getValue();
          final String appName = attributes.get("application_name").getStringValue().getValue();


          final EVSpan span = new EVSpan(spanId, traceId, startTime, endTime, duration,
              operationName, 1, hostname, appName);

          result.add(KeyValue.pair(traceId, span));
        }

      } catch (final IOException e) {
        e.printStackTrace();
      }

      return result;
    });


    traceIdSpanStream.to(this.config.getOutTopic(),
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

