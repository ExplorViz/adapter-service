package net.explorviz.adapter.validation;

import net.explorviz.avro.EVSpan;

/**
 * Validates and possibly manipulates {@link EVSpan}s prior to processing.
 */
public interface SpanValidator {

  /**
   * Checks if the given span is valid and throws if not.
   *
   * @param span the span
   * @throws InvalidSpanException if the span is invalid and can't be recovered
   */
  void validate(EVSpan span) throws InvalidSpanException;

  /**
   * Same as {@link #validate(EVSpan)} but instead of throwing, this method returns a boolean value
   * that reflects the validity of the span.
   * @param span the span
   * @return true iff the span is valid
   */
  boolean isValid(EVSpan span);




}
