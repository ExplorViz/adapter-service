package net.explorviz.adapter.service.validation;

import net.explorviz.avro.SpanStructure;

public interface SpanStructureSanitizer {

  /**
   * Fixes missing and/or malformed properties of spans
   *
   * @param span span to sanitize
   * @return a santitzed version of the given sapn
   */
  SpanStructure sanitize(SpanStructure span);
}
