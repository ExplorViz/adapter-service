package net.explorviz.adapter.service.converter;

import io.opencensus.proto.trace.v1.Span;
import javax.enterprise.context.ApplicationScoped;
import net.explorviz.avro.SpanStructure;
import net.explorviz.avro.Timestamp;

/**
 * Converts {@link Span}s to {@link SpanStructure}s.
 */
@ApplicationScoped
public class SpanStructureConverter implements SpanConverter<SpanStructure> {

  @Override
  public SpanStructure fromOpenCensusSpan(final Span ocSpan) {
    final String spanId = IdHelper.converterSpanId(ocSpan.getSpanId().toByteArray());

    final Timestamp startTime =
        new Timestamp(ocSpan.getStartTime().getSeconds(), ocSpan.getStartTime().getNanos());

    final SpanStructure.Builder builder = SpanStructure.newBuilder();
    builder
        .setSpanId(spanId)
        .setTimestamp(startTime);


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
