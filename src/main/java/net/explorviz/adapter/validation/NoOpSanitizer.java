package net.explorviz.adapter.validation;

import net.explorviz.avro.EVSpan;

/**
 * Does not actually sanitize, for testing purposes.
 */
public class NoOpSanitizer implements SpanSanitizer {
  @Override
  public EVSpan sanitize(EVSpan span) {
    return span;
  }
}
