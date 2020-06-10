package net.explorviz.adapter.kafka;

import com.fasterxml.jackson.databind.util.ArrayIterator;
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

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

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

@ApplicationScoped
public class SpanTranslatorStream {

  private final SchemaRegistryClient registry;

  private final KafkaConfig config;

  private final Properties streamsConfig = new Properties();

  private Topology topology;

  private SpanValidator validator;
  private SpanSanitizer sanitizer;

  private KafkaStreams streams;

  @Inject
  public SpanTranslatorStream(final SchemaRegistryClient registry, final KafkaConfig config,
      final SpanValidator validator, final SpanSanitizer sanitizer) {
    this.registry = registry;
    this.config = config;
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
      EVSpan span = toEVSpan(s);
      return new KeyValue<>(span.getTraceId(), span);
    }).mapValues(s -> sanitizer.sanitize(s));

    final KStream<String, EVSpan> validEVSpanStream = traceIdEVSpanStream.filter(($, v) -> validator.isValid(v));
    final KStream<String, EVSpan> invalidEVSpanStream = traceIdEVSpanStream.filterNot(($, v) -> validator.isValid(v));

    validEVSpanStream.to(this.config.getOutTopic(),
        Produced.with(Serdes.String(), this.getValueSerde()));

    // TODO: invalidEVSpanStream to Event Messages


    this.topology = builder.build();
  }


  /**
   * Converts a {@link Span} to an {@link EVSpan}
   * @param original the original span
   * @return the converted span
   */
  private EVSpan toEVSpan(Span original) {
    final String traceId =
        BaseEncoding.base16().lowerCase().encode(original.getTraceId().toByteArray(), 0, 16);

    final String spanId =
        BaseEncoding.base16().lowerCase().encode(original.getSpanId().toByteArray(), 0, 8);

    final Timestamp startTime =
        new Timestamp(original.getStartTime().getSeconds(), original.getStartTime().getNanos());

    final long endTime = Instant
        .ofEpochSecond(original.getEndTime().getSeconds(), original.getEndTime().getNanos())
        .toEpochMilli();


    final long duration =
        endTime - Duration.ofSeconds(startTime.getSeconds(), startTime.getNanoAdjust()).toMillis();

    final Map<String, AttributeValue> attributes = original.getAttributes().getAttributeMapMap();
    final String operationName = attributes.get("method_fqn").getStringValue().getValue();
    final String hostname = attributes.get("host").getStringValue().getValue();
    final String appName = attributes.get("application_name").getStringValue().getValue();


    return EVSpan
        .newBuilder()
        .setLandscapeToken("TOK")
        .setRequestCount(1)
        .setSpanId(spanId)
        .setTraceId(traceId)
        .setStartTime(startTime)
        .setEndTime(endTime)
        .setDuration(duration)
        .setHostname(hostname)
        .setHostIpAddress("0.0.0.0")
        .setAppName(appName)
        .setAppLanguage("LANG")
        .setAppPid("PID")
        .setOperationName(operationName)
        .build();

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

