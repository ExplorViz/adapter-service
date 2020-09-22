package net.explorviz.adapter.conversion;

import net.explorviz.avro.SpanStructure;
import net.explorviz.avro.Timestamp;

public abstract class SpanStructureConverter<T> {

  protected abstract Timestamp startTime(T span);
  protected abstract Timestamp endTime(T span);
  protected abstract String spanId(T span);
  protected abstract SpanAttributes spanAttributes(T span);



  /**
   * Converts a {@link T} to an {@link SpanStructure}
   *
   * @param original the original span
   * @return the converted span
   */
  public final SpanStructure toSpanStructure(final T original) {

    final String spanId = spanId(original);

    final SpanStructure.Builder builder = SpanStructure.newBuilder();
    builder
        .setSpanId(spanId)
        .setTimestamp(startTime(original));

    SpanAttributes spanAttributes = spanAttributes(original);
    spanAttributes.appendToStructure(builder);

    // temporary hash code since the field is required for avro builder
    builder.setHashCode("");

    final SpanStructure span = builder.build();

    // HashCode is used to map structural and dynamic data
    final String hashCode = HashHelper.fromSpanAttributes(spanAttributes);
    span.setHashCode(hashCode);

    return span;
  }


}
