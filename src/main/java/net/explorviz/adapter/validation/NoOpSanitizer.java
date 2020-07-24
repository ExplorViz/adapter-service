package net.explorviz.adapter.validation;

import javax.enterprise.context.ApplicationScoped;
import net.explorviz.avro.EVSpan;

/**
 * Does not actually sanitize, for testing purposes.
 */
@ApplicationScoped
public class NoOpSanitizer implements SpanSanitizer {
  @Override
  public EVSpan sanitize(final EVSpan span) {
    return span;
  }
}
