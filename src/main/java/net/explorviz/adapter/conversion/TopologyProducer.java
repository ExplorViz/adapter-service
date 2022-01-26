package net.explorviz.adapter.conversion;

import com.google.protobuf.InvalidProtocolBufferException;
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import io.opencensus.proto.dump.DumpSpans;
import io.opencensus.proto.trace.v1.Span;
import io.quarkus.scheduler.Scheduled;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import net.explorviz.adapter.service.converter.SpanDynamicConverter;
import net.explorviz.adapter.service.converter.SpanStructureConverter;
import net.explorviz.adapter.service.validation.SpanValidator;
import net.explorviz.avro.SpanDynamic;
import net.explorviz.avro.SpanStructure;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Produced;
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

  @ConfigProperty(name = "explorviz.kafka-streams.topics.in")
  /* default */ String inTopic; // NOCS

  @ConfigProperty(name = "explorviz.kafka-streams.topics.out.structure")
  /* default */ String structureOutTopic; // NOCS

  @ConfigProperty(name = "explorviz.kafka-streams.topics.out.dynamic")
  /* default */ String dynamicOutTopic; // NOCS

  @Inject
  /* default */ SpanValidator validator; // NOCS

  @Inject
  /* default */ SpecificAvroSerde<SpanDynamic> dynamicAvroSerde; // NOCS

  @Inject
  /* default */ SpecificAvroSerde<SpanStructure> structureAvroSerde; // NOCS

  @Inject
  /* default */ SchemaRegistryClient schemaRegistryClient; // NOCS

  @Inject
  /* default */ SpanStructureConverter structureConverter; // NOCS

  @Inject
  /* default */ SpanDynamicConverter dynamicConverter; // NOCS

  // Logged and reset every n seconds
  private final AtomicInteger lastReceivedSpans = new AtomicInteger(0);
  private final AtomicInteger lastInvalidSpans = new AtomicInteger(0);

  @Produces
  public Topology buildTopology() {

    final StreamsBuilder builder = new StreamsBuilder();

    final KStream<byte[], byte[]> dumpSpanStream =
        builder.stream(this.inTopic, Consumed.with(Serdes.ByteArray(), Serdes.ByteArray()));

    final KStream<byte[], Span> spanKStream = dumpSpanStream.flatMapValues(d -> {
      try {
        final List<Span> spanList = DumpSpans.parseFrom(d).getSpansList();

        this.lastReceivedSpans.addAndGet(spanList.size());

        return spanList;
      } catch (final InvalidProtocolBufferException e) {
        return new ArrayList<>();
      }
    });

    // Validate Spans
    final KStream<byte[], Span> validSpanStream = spanKStream.filter((k, v) -> {
      return this.validator.isValid(v);
    });

    // Invalid Spans, just log
    spanKStream.filter((k, v) -> !this.validator.isValid(v))
        .foreach((k, v) -> this.lastInvalidSpans.incrementAndGet());

    // Convert to Span Structure
    final KStream<String, SpanStructure> spanStructureStream = validSpanStream.map((key, value) -> {
      final SpanStructure span = this.structureConverter.fromOpenCensusSpan(value);
      return new KeyValue<>(span.getLandscapeToken(), span);
    });

    // Convert to Span Dynamic
    final KStream<String, SpanDynamic> spanDynamicStream = validSpanStream.map((key, value) -> {
      final SpanDynamic dynamic = this.dynamicConverter.fromOpenCensusSpan(value);
      return new KeyValue<>(dynamic.getTraceId(), dynamic);
    });

    // Forward Span Structure
    spanStructureStream.to(this.structureOutTopic,
        Produced.with(Serdes.String(), this.structureAvroSerde));

    // Forward Span Dynamic
    spanDynamicStream.to(this.dynamicOutTopic,
        Produced.with(Serdes.String(), this.dynamicAvroSerde));

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
