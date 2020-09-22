package net.explorviz.adapter.conversion.opencensus.converter;

import io.opencensus.proto.trace.v1.Span;
import javax.enterprise.context.ApplicationScoped;
import net.explorviz.adapter.conversion.HashHelper;
import net.explorviz.adapter.conversion.IdHelper;
import net.explorviz.adapter.conversion.SpanAttributes;
import net.explorviz.adapter.conversion.SpanDynamicConverter;
import net.explorviz.avro.SpanDynamic;
import net.explorviz.avro.Timestamp;

/**
 * Converts a {@link Span} to a {@link SpanDynamic}.
 */
@ApplicationScoped
public class OcSpanDynamicConverter extends SpanDynamicConverter<Span> {

  @Override
  protected Timestamp startTime(final Span span) {
    return new Timestamp(span.getStartTime().getSeconds(), span.getStartTime().getNanos());
  }

  @Override
  protected Timestamp endTime(final Span span) {
    return new Timestamp(span.getEndTime().getSeconds(), span.getEndTime().getNanos());
  }

  @Override
  protected String spanId(final Span span) {
    return IdHelper.converterSpanId(span.getSpanId().toByteArray());
  }

  @Override
  protected String traceId(final Span span) {
    return IdHelper.converterTraceId(span.getTraceId().toByteArray());
  }

  @Override
  protected SpanAttributes spanAttributes(final Span span) {
    return new OcSpanAttributes(span);
  }

  @Override
  protected String parentSpanId(final Span span) {
    String parentSpan = "";
    if (span.getParentSpanId().size() > 0) {
      parentSpan = IdHelper.converterSpanId(span.getParentSpanId().toByteArray());
    }
    return parentSpan;
  }

}
