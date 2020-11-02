package net.explorviz.adapter.conversion.transformer;

import io.opencensus.proto.trace.v1.Span;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import net.explorviz.adapter.conversion.converter.SpanDynamicConverter;
import net.explorviz.avro.SpanDynamic;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.Transformer;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class DynamicTransformer
    implements Transformer<byte[], Span, KeyValue<String, SpanDynamic>> {


  private final static Logger LOGGER = LoggerFactory.getLogger(DynamicTransformer.class);
  private final SpanDynamicConverter converter;


  @Inject
  public DynamicTransformer(final SpanDynamicConverter converter) {
    this.converter = converter;
  }

  @Override
  public void init(final ProcessorContext context) {

  }

  @Override
  public KeyValue<String, SpanDynamic> transform(final byte[] key, final Span value) {
    SpanDynamic dynamic = converter.toSpanDynamic(value);
    return new KeyValue<>(dynamic.getTraceId(), dynamic);
  }

  @Override
  public void close() {

  }
}
