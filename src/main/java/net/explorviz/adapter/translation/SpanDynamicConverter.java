package net.explorviz.adapter.translation;

import io.opencensus.proto.trace.v1.Span;
import javax.enterprise.context.ApplicationScoped;
import net.explorviz.avro.SpanDynamic;
import net.explorviz.avro.Timestamp;

/**
 * Converts a {@link Span} to a {@link SpanDynamic}.
 */
@ApplicationScoped
public class SpanDynamicConverter {

  /**
   * Converts a {@link Span} to a {@link SpanDynamic}.
   */
  public SpanDynamic toSpanDynamic(Span span) {

    final Timestamp startTime =
        new Timestamp(span.getStartTime().getSeconds(), span.getStartTime().getNanos());

    final Timestamp endTime =
        new Timestamp(span.getEndTime().getSeconds(), span.getEndTime().getNanos());

    AttributesReader attributesReader = new AttributesReader(span);

    String parentSpan = "";
    if (span.getParentSpanId().size() > 0) {
      parentSpan = IdHelper.converterSpanId(span.getParentSpanId().toByteArray());
    }



    SpanDynamic spanDynamic =  SpanDynamic.newBuilder()
        .setLandscapeToken(attributesReader.getLandscapeToken())
        .setParentSpanId(parentSpan)
        .setSpanId(IdHelper.converterSpanId(span.getSpanId().toByteArray()))
        .setTraceId(IdHelper.converterTraceId(span.getTraceId().toByteArray()))
        .setHashCode("") // temporary hash code since the field is required for avro builder
        .setStartTime(startTime)
        .setEndTime(endTime)
        .build();

    String hashValue = HashHelper.fromSpanAttributes(new AttributesReader(span));
    spanDynamic.setHashCode(hashValue);
    return spanDynamic;
  }

}
