package net.explorviz.adapter.translation;


import io.opentelemetry.proto.trace.v1.Span;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAccessor;
import javax.enterprise.context.ApplicationScoped;
import net.explorviz.avro.SpanDynamic;
import net.explorviz.avro.Timestamp;
import org.joda.time.DateTime;

/**
 * Converts a {@link Span} to a {@link SpanDynamic}.
 */
@ApplicationScoped
public class SpanDynamicConverter {

  /**
   * Converts a {@link Span} to a {@link SpanDynamic}.
   */
  public SpanDynamic toSpanDynamic(Span span) {

    Instant startInstant = Instant.EPOCH.plus(span.getStartTimeUnixNano(), ChronoUnit.NANOS);
    Instant endInstant = Instant.EPOCH.plus(span.getEndTimeUnixNano(), ChronoUnit.NANOS);

    final Timestamp startTime =
        new Timestamp(startInstant.getEpochSecond(), startInstant.getNano());

    final Timestamp endTime = new Timestamp(endInstant.getEpochSecond(), endInstant.getNano());

    SpanAttributes spanAttributes = new SpanAttributes(span);

    String parentSpan = "";
    if (span.getParentSpanId().size() > 0) {
      parentSpan = IdHelper.converterSpanId(span.getParentSpanId().toByteArray());
    }



    SpanDynamic spanDynamic =  SpanDynamic.newBuilder()
        .setLandscapeToken(spanAttributes.getLandscapeToken())
        .setParentSpanId(parentSpan)
        .setSpanId(IdHelper.converterSpanId(span.getSpanId().toByteArray()))
        .setTraceId(IdHelper.converterTraceId(span.getTraceId().toByteArray()))
        .setHashCode("") // temporary hash code since the field is required for avro builder
        .setStartTime(startTime)
        .setEndTime(endTime)
        .build();

    String hashValue = HashHelper.fromSpanAttributes(spanAttributes);
    spanDynamic.setHashCode(hashValue);
    return spanDynamic;
  }

}
