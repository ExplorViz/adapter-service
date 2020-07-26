package net.explorviz.adapter.validation;

import javax.enterprise.context.ApplicationScoped;
import net.explorviz.avro.SpanStructure;

/**
 * Does not actually sanitize, for testing purposes.
 */
@ApplicationScoped
public class NoOpSanitizer implements SpanSanitizer {
  @Override
  public SpanStructure sanitize(final SpanStructure span) {
    return span;
  }
}
