package net.explorviz.adapter.conversion;

import io.opentelemetry.proto.trace.v1.Span;
import net.explorviz.avro.SpanDynamic;
import net.explorviz.avro.Timestamp;

public abstract class SpanDynamicConverter<T> {

  protected abstract Timestamp startTime(T span);
  protected abstract Timestamp endTime(T span);
  protected abstract String spanId(T span);
  protected abstract String traceId(T span);
  protected abstract SpanAttributes spanAttributes(T span);
  protected abstract String parentSpanId(T span);

  /**
   * Converts a {@link Span} to a {@link SpanDynamic}.
   */
  public final SpanDynamic toSpanDynamic(T span) {

    SpanAttributes spanAttributes = spanAttributes(span);
    String parentSpan = parentSpanId(span);
    SpanDynamic spanDynamic = SpanDynamic.newBuilder()
        .setLandscapeToken(spanAttributes.getLandscapeToken())
        .setParentSpanId(parentSpan)
        .setSpanId(spanId(span))
        .setTraceId(traceId(span))
        .setHashCode("") // temporary hash code since the field is required for avro builder
        .setStartTime(startTime(span))
        .setEndTime(endTime(span))
        .build();

    String hashValue = HashHelper.fromSpanAttributes(spanAttributes);
    spanDynamic.setHashCode(hashValue);
    return spanDynamic;
  }
}
