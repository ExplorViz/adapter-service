package net.explorviz.adapter.service.validation;

import io.opentelemetry.proto.trace.v1.Span;

/**
 * Validates [Span]s prior to processing.
 */
interface SpanValidator {

  /**
   * Checks if the given span is valid.
   *
   * @param span the span
   * @return Boolean that indicates if span is valid.
   */
  fun isValid(span: Span): Boolean
}

