package net.explorviz.adapter.conversion.transformer;

import io.opencensus.proto.trace.v1.Span;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import net.explorviz.adapter.service.converter.SpanDynamicConverter;
import net.explorviz.avro.SpanDynamic;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.Transformer;
import org.apache.kafka.streams.processor.ProcessorContext;

/**
 * Kafka-Streams transformer which builds a {@link SpanDynamic} out of a {@link Span}.
 */
@ApplicationScoped
public class DynamicTransformer
    implements Transformer<byte[], Span, KeyValue<String, SpanDynamic>> {

  private final SpanDynamicConverter converter;

  @Inject
  public DynamicTransformer(final SpanDynamicConverter converter) {
    this.converter = converter;
  }

  @Override
  public void init(final ProcessorContext context) {
    // Nothing to do
  }

  @Override
  public KeyValue<String, SpanDynamic> transform(final byte[] key, final Span value) {
    final SpanDynamic dynamic = this.converter.fromOpenCensusSpan(value);
    return new KeyValue<>(dynamic.getTraceId(), dynamic);
  }

  @Override
  public void close() {
    // Nothing to do
  }
}
