package net.explorviz.adapter.conversion;

import com.google.protobuf.InvalidProtocolBufferException;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import io.opentelemetry.proto.collector.trace.v1.ExportTraceServiceRequest;
import io.opentelemetry.proto.trace.v1.Span;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import net.explorviz.adapter.service.converter.SpanConverterImpl;
import net.explorviz.adapter.service.validation.SpanValidator;
import net.explorviz.avro.EventType;
import net.explorviz.avro.TokenEvent;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.state.KeyValueStore;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Builds a KafkaStream topology instance with all its transformers. Entry point of the stream
 * analysis.
 */
@ApplicationScoped
public class TopologyProducer {

  private static final Logger LOGGER = LoggerFactory.getLogger(TopologyProducer.class);
  // Logged and reset every n seconds
  private final AtomicInteger lastReceivedSpans = new AtomicInteger(0);
  private final AtomicInteger lastInvalidSpans = new AtomicInteger(0);
  @ConfigProperty(name = "explorviz.kafka-streams.topics.in")
  /* default */ String inTopic; // NOCS
  @ConfigProperty(name = "explorviz.kafka-streams.topics.out.spans")
  /* default */ String spansOutTopic; // NOCS
  @ConfigProperty(name = "explorviz.kafka-streams.topics.in.tokens")
  /* default */ String tokensInTopic; // NOCS
  @ConfigProperty(name = "explorviz.kafka-streams.topics.out.tokens-table")
  /* default */ String tokensOutTopic; // NOCS
  @Inject
  /* default */ SpanValidator validator; // NOCS
  @Inject
  /* default */ SpecificAvroSerde<net.explorviz.avro.Span> spanAvroSerde; // NOCS
  @Inject
  /* default */ SpecificAvroSerde<TokenEvent> tokenEventAvroSerde; // NOCS
  @Inject
  /* default */ SpanConverterImpl spanConverter; // NOCS

  @Produces
  public Topology buildTopology() {

    final StreamsBuilder builder = new StreamsBuilder();

    // BEGIN Conversion Stream

    final KStream<byte[], byte[]> spanByteStream =
        builder.stream(this.inTopic, Consumed.with(Serdes.ByteArray(), Serdes.ByteArray()));

    final KStream<byte[], Span> spanStream = spanByteStream.flatMapValues(d -> {
      try {

        final List<Span> spanList = new ArrayList<>();

        ExportTraceServiceRequest.parseFrom(d).getResourceSpansList()
            .forEach(resourceSpans -> resourceSpans.getScopeSpansList()
                .forEach(scopeSpans -> spanList.addAll(scopeSpans.getSpansList())));

        this.lastReceivedSpans.addAndGet(spanList.size());

        return spanList;
      } catch (final InvalidProtocolBufferException e) {
        return Collections.emptyList();
      }
    });

    // Validate Spans
    final KStream<byte[], Span> validSpanStream = spanStream.flatMapValues((key, value) -> {
      if (!this.validator.isValid(value)) {
        this.lastInvalidSpans.incrementAndGet();
        return Collections.emptyList();
      }

      return Collections.singletonList(value);
    });

    // Convert to Span Structure
    final KStream<String, net.explorviz.avro.Span> explorvizSpanStream = validSpanStream.map(
        (key, value) -> {
          final net.explorviz.avro.Span span = this.spanConverter.fromOpenTelemetrySpan(value);
          System.out.print(" FQN: " + span.getFullyQualifiedOperationName());
          return new KeyValue<>(span.getLandscapeToken(), span);
        });

    // Forward Spans (general purpose event)
    explorvizSpanStream.to(this.spansOutTopic,
        Produced.with(Serdes.String(), this.spanAvroSerde));

    // END Conversion Stream

    // BEGIN Token Stream

    builder.stream(this.tokensInTopic, Consumed.with(Serdes.String(), this.tokenEventAvroSerde))
        .filter((key, value) -> {
          if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("Received token event for token value {} with event {}", key, value);
          }
          return value == null || value.getType().equals(EventType.CREATED);
        }).to(this.tokensOutTopic, Produced.with(Serdes.String(), this.tokenEventAvroSerde));

    builder.globalTable(this.tokensOutTopic,
        Materialized
            .<String, TokenEvent, KeyValueStore<Bytes, byte[]>>as("token-events-global-store")
            .withKeySerde(Serdes.String()).withValueSerde(this.tokenEventAvroSerde));

    // END Token Stream

    return builder.build();
  }

  @Scheduled(every = "{explorviz.log.span.interval}")
    /* default */ void logStatus() {
    final int spans = this.lastReceivedSpans.getAndSet(0);
    final int invalidSpans = this.lastInvalidSpans.getAndSet(0);
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("Received {} spans: {} valid, {} invalid ", spans, spans - invalidSpans,
          invalidSpans);
    }
  }

}
