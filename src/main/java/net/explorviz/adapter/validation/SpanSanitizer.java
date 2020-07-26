package net.explorviz.adapter.validation;

import net.explorviz.avro.SpanStructure;

public interface SpanSanitizer {

  /**
   * Fixes missing and/or malformed properties of spans
   *
   * @param span span to sanitize
   * @return a santitzed version of the given sapn
   */
  SpanStructure sanitize(SpanStructure span);
}
