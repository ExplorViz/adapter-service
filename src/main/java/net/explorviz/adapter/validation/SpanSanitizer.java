package net.explorviz.adapter.validation;

import net.explorviz.avro.EVSpan;

public interface SpanSanitizer {

  /**
   * Fixes missing and/or malformed properties of spans
   *
   * @param span span to sanitize
   * @return a santitzed version of the given sapn
   */
  EVSpan sanitize(EVSpan span);
}
