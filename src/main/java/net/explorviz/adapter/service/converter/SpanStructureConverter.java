package net.explorviz.adapter.service.converter;

import io.opentelemetry.proto.trace.v1.Span;
import javax.enterprise.context.ApplicationScoped;
import net.explorviz.avro.SpanStructure;

/**
 * Converts {@link Span}s to {@link SpanStructure}s.
 */
@ApplicationScoped
public class SpanStructureConverter implements SpanConverter<SpanStructure> {

  private static final long TO_MILLISEC_DIVISOR = 1_000_000L;

  @Override
  public SpanStructure fromOpenCensusSpan(final Span ocSpan) {
    final String spanId = IdHelper.converterSpanId(ocSpan.getSpanId().toByteArray());

    final SpanStructure.Builder builder = SpanStructure.newBuilder();
    builder
        .setSpanId(spanId)
        .setTimestampInEpochMilli(ocSpan.getStartTimeUnixNano() / TO_MILLISEC_DIVISOR);

    final AttributesReader attributesReader = new AttributesReader(ocSpan);
    attributesReader.appendToStructure(builder);

    // temporary hash code since the field is required for avro builder
    builder.setHashCode("");

    final SpanStructure span = builder.build();

    // HashCode is used to map structural and dynamic data
    final String hashCode = HashHelper.fromSpanAttributes(attributesReader);
    span.setHashCode(hashCode);

    return span;
  }
}
