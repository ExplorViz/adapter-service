package net.explorviz.adapter.validation;

import net.explorviz.avro.EVSpan;

/**
 * Thrown if a {@link EVSpan} is invalid.
 */
public class InvalidSpanException extends Exception {

  private EVSpan span;

  public InvalidSpanException(String message, EVSpan span) {
    super(message);
    this.span = span;
  }

  public InvalidSpanException(String message, Throwable cause, EVSpan span) {
    super(message, cause);
    this.span = span;
  }

  public EVSpan getSpan() {
    return span;
  }
}
