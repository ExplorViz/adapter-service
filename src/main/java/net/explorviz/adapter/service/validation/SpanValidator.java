package net.explorviz.adapter.service.validation;

import io.opentelemetry.proto.trace.v1.Span;

/**
 * Validates {@link Span}s prior to processing.
 */
public interface SpanValidator {

  /**
   * Checks if the given span is valid.
   *
   * @param span the span
   * @return Boolean that indicates if span is valid.
   */
  boolean isValid(Span span);
  
}
