package net.explorviz.adapter.validation;

import net.explorviz.avro.EVSpan;

/**
 * Validates and possibly manipulates {@link EVSpan}s prior to processing.
 */
public interface SpanValidator {

  /**
   * Checks if the given span is valid.
   *
   * @param span the span
   * @return Boolean that indicates if span is valid.
   */
  boolean isValid(EVSpan span);


}
