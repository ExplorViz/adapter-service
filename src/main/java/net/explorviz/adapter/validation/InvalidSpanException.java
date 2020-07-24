package net.explorviz.adapter.validation;

import net.explorviz.avro.EVSpan;

/**
 * Thrown if a {@link EVSpan} is invalid.
 */
public class InvalidSpanException extends Exception {

  private final EVSpan span;

  public InvalidSpanException(final String message, final EVSpan span) {
    super(message);
    this.span = span;
  }

  public InvalidSpanException(final String message, final Throwable cause, final EVSpan span) {
    super(message, cause);
    this.span = span;
  }

  public EVSpan getSpan() {
    return this.span;
  }
}
