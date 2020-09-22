package net.explorviz.adapter.conversion.opentelemetry.converter;

import io.opentelemetry.proto.trace.v1.Span;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import javax.enterprise.context.ApplicationScoped;
import net.explorviz.adapter.conversion.IdHelper;
import net.explorviz.adapter.conversion.SpanAttributes;
import net.explorviz.adapter.conversion.SpanStructureConverter;
import net.explorviz.avro.SpanStructure;
import net.explorviz.avro.Timestamp;

/**
 * Converts {@link Span}s to {@link SpanStructure}s.
 */
@ApplicationScoped
public class OtSpanStructureConverter extends SpanStructureConverter<Span> {

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
  protected SpanAttributes spanAttributes(final Span span) {
    return new OtSpanAttributes(span);
  }
}
