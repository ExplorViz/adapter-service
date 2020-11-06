package net.explorviz.adapter.service.validation;

import javax.enterprise.context.ApplicationScoped;
import net.explorviz.avro.SpanStructure;

/**
 * Does not actually sanitize, for testing purposes.
 */
@ApplicationScoped
public class NoOpStructureSanitizer implements SpanStructureSanitizer {
  @Override
  public SpanStructure sanitize(final SpanStructure span) {
    return span;
  }
}
