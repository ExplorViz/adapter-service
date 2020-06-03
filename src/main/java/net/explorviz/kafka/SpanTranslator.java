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
  public SpanTranslator(SchemaRegistryClient registry, KafkaConfig config) {
    this.registry = registry;
    this.config = config;
  }
 
  void onStart(@Observes StartupEvent event) {
    setupStreamsConfig();
    buildTopology();
    
    streams = new KafkaStreams(this.topology, streamsConfig);
    streams.cleanUp();
    streams.start();
  }

  void onStop(@Observes ShutdownEvent event) {
    streams.close();
  }

  private void setupStreamsConfig() {
    streamsConfig.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, config.getBootstrapServers());
    streamsConfig.put(StreamsConfig.APPLICATION_ID_CONFIG, config.getApplicationId());
  }

  private void buildTopology() {

    StreamsBuilder builder = new StreamsBuilder();

    // Stream 1

    KStream<byte[], byte[]> dumpSpanStream =
        builder.stream(config.getInTopic(), Consumed.with(Serdes.ByteArray(), Serdes.ByteArray()));

    KStream<String, EVSpan> traceIdSpanStream = dumpSpanStream.flatMap((key, value) -> {

      DumpSpans dumpSpan;
      List<KeyValue<String, EVSpan>> result = new LinkedList<>();
      try {

        dumpSpan = DumpSpans.parseFrom(value);

        for (Span s : dumpSpan.getSpansList()) {
          String traceId =
              BaseEncoding.base16().lowerCase().encode(s.getTraceId().toByteArray(), 0, 16);

          String spanId =
              BaseEncoding.base16().lowerCase().encode(s.getSpanId().toByteArray(), 0, 8);



          Timestamp startTime =
              new Timestamp(s.getStartTime().getSeconds(), s.getStartTime().getNanos());

          long endTime = Instant
              .ofEpochSecond(s.getEndTime().getSeconds(), s.getEndTime().getNanos()).toEpochMilli();


          long duration = endTime
              - Duration.ofSeconds(startTime.getSeconds(), startTime.getNanoAdjust()).toMillis();



          // System.out.println(startTime + " und " + s.getStartTime().getSeconds());

          // System.out.println(Duration
          // .ofNanos(Duration
          // .ofSeconds(s.getStartTime().getSeconds(), s.getStartTime().getNanos()).toNanos())
          // .toMillis());

          // System.out.println(duration + " und " + Duration.between(t, t1).getNano());

          Map<String, AttributeValue> attributes = s.getAttributes().getAttributeMapMap();
          String operationName = attributes.get("method_fqn").getStringValue().getValue();
          String hostname = attributes.get("host").getStringValue().getValue();
          String appName = attributes.get("application_name").getStringValue().getValue();


          EVSpan span = new EVSpan(spanId, traceId, startTime, endTime, duration, operationName, 1,
              hostname, appName);

          result.add(KeyValue.pair(traceId, span));
        }

      } catch (IOException e) {
        e.printStackTrace();
      }

      return result;


    });

    
    
    //traceIdSpanStream.to(config.getOutTopic(), Produced.with(Serdes.String(), new SpecificAvroSerde<EVSpan>()));
    //traceIdSpanStream.to(config.getOutTopic(), Produced.valueSerde(new SpecificAvroSerde<EVSpan>()).keySerde(Serdes.String()));
    //traceIdSpanStream.to(config.getOutTopic());
    
    this.topology = builder.build();
  }

  public Topology getTopology() {
    return topology;
  }

  private <T extends SpecificRecord> SpecificAvroSerde<T> getValueSerde() {
    final SpecificAvroSerde<T> valueSerde = new SpecificAvroSerde<>(registry);
    valueSerde.configure(
        Map.of(AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, "http://localhost:8081"),
        false);
    return valueSerde;
  }

}

