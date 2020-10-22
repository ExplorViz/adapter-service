package net.explorviz.adapter.translation;

import io.opencensus.proto.trace.v1.Span;
import javax.enterprise.context.ApplicationScoped;
import net.explorviz.avro.SpanStructure;
import net.explorviz.avro.Timestamp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Converts {@link Span}s to {@link SpanStructure}s.
 */
@ApplicationScoped
public class SpanStructureConverter {

  private static final Logger LOGGER = LoggerFactory.getLogger(SpanStructureConverter.class);



  /**
   * Converts a {@link Span} to an {@link SpanStructure}
   *
   * @param original the original span
   * @return the converted span
   */
  public SpanStructure toSpanStructure(final Span original) {

    final String spanId = IdHelper.converterSpanId(original.getSpanId().toByteArray());

    final Timestamp startTime =
        new Timestamp(original.getStartTime().getSeconds(), original.getStartTime().getNanos());

    final SpanStructure.Builder builder = SpanStructure.newBuilder();
    builder
        .setSpanId(spanId)
        .setTimestamp(startTime);

    final AttributesReader attributesReader = new AttributesReader(original);
    attributesReader.appendToStructure(builder);

    // temporary hash code since the field is required for avro builder
    builder.setHashCode("");

    final SpanStructure span = builder.build();

    // HashCode is used to map structural and dynamic data
    final String hashCode = HashHelper.fromSpanAttributes(attributesReader);
    span.setHashCode(hashCode);

    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("Converted SpanStructure: {}", span.toString());
    }

    return span;
  }

}
