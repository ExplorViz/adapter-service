package net.explorviz.adapter.conversion.opentelemetry.converter;


import io.opentelemetry.proto.trace.v1.Span;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
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
public class OtSpanDynamicConverter extends SpanDynamicConverter<Span> {



  @Override
  protected Timestamp startTime(final Span span) {
    Instant startInstant = Instant.EPOCH.plus(span.getStartTimeUnixNano(), ChronoUnit.NANOS);
    return new Timestamp(startInstant.getEpochSecond(), startInstant.getNano());
  }

  @Override
  protected Timestamp endTime(final Span span) {
    Instant endInstant = Instant.EPOCH.plus(span.getEndTimeUnixNano(), ChronoUnit.NANOS);
    return new Timestamp(endInstant.getEpochSecond(), endInstant.getNano());
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
    return new OtSpanAttributes(span);
  }

  @Override
  protected String parentSpanId(final Span span) {
    if (span.getParentSpanId().size() > 0) {
      return IdHelper.converterSpanId(span.getParentSpanId().toByteArray());
    } else {
      return "";
    }
  }
}
