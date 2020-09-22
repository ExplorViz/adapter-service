package net.explorviz.adapter.conversion.opencensus.stream;

import io.opencensus.proto.trace.v1.Span;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import net.explorviz.adapter.conversion.opencensus.converter.OcSpanDynamicConverter;
import net.explorviz.adapter.util.PerfomanceLogger;
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
  private PerfomanceLogger perfLogger =
      PerfomanceLogger.newOperationPerformanceLogger(LOGGER, 1000, "Converted {} spans in {} ms");

  private final OcSpanDynamicConverter converter;


  @Inject
  public DynamicTransformer(final OcSpanDynamicConverter converter) {
    this.converter = converter;
  }

  @Override
  public void init(final ProcessorContext context) {

  }

  @Override
  public KeyValue<String, SpanDynamic> transform(final byte[] key, final Span value) {
    SpanDynamic dynamic = converter.toSpanDynamic(value);
    perfLogger.logOperation();
    return new KeyValue<>(dynamic.getTraceId(), dynamic);
  }

  @Override
  public void close() {

  }
}
