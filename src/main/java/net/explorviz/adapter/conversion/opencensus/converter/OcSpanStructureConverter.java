package net.explorviz.adapter.conversion.opencensus.converter;

import io.opencensus.proto.trace.v1.Span;
import javax.enterprise.context.ApplicationScoped;
import net.explorviz.adapter.conversion.HashHelper;
import net.explorviz.adapter.conversion.IdHelper;
import net.explorviz.adapter.conversion.SpanAttributes;
import net.explorviz.adapter.conversion.SpanStructureConverter;
import net.explorviz.avro.SpanStructure;
import net.explorviz.avro.Timestamp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Converts {@link Span}s to {@link SpanStructure}s.
 */
@ApplicationScoped
public class OcSpanStructureConverter extends SpanStructureConverter<Span> {

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
  protected SpanAttributes spanAttributes(final Span span) {
    return new OcSpanAttributes(span);
  }
}
