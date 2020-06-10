package net.explorviz.adapter.validation;


import javax.enterprise.context.ApplicationScoped;
import net.explorviz.avro.EVSpan;

import java.time.DateTimeException;
import java.time.Instant;

/**
 * Validator that enforces that all values of the {@link EVSpan} are set and valid.
 * Does not sanitize invalid attributes with default values.
 */
@ApplicationScoped
public class StrictValidator implements SpanValidator {

  @Override
  public void validate(EVSpan span) throws InvalidSpanException {

    if (span.getLandscapeToken() == null || span.getLandscapeToken().isBlank()) {
      throw new InvalidSpanException("No token", span);
    }

    validateTimestamp(span);
    validateHost(span);
    validateApp(span);
    validateOperation(span);
  }

  @Override
  public boolean isValid(EVSpan span) {
    try {
      validate(span);
      return true;
    } catch (InvalidSpanException e) {
      return false;
    }
  }

  private void validateTimestamp(EVSpan span) throws InvalidSpanException {
    try {
      Instant ignored = Instant.ofEpochSecond(span.getStartTime().getSeconds(),
          span.getStartTime().getNanoAdjust());
      if (ignored.getEpochSecond() <= 0) {
        throw new NumberFormatException("Time must be positive");
      }
    } catch (DateTimeException | NumberFormatException e) {
      throw new InvalidSpanException("Invalid timestamp", e, span);
    }
  }

  private void validateHost(EVSpan span) throws InvalidSpanException {
    if (isBlank(span.getHostname())) {
      throw new InvalidSpanException("No hostname", span);
    }
    if (isBlank(span.getHostIpAddress())) {
      throw new InvalidSpanException("No IP address", span);
    }
  }

  private void validateApp(EVSpan span) throws InvalidSpanException {
    if (isBlank(span.getAppName())) {
      throw new InvalidSpanException("No application name", span);
    }

    if (isBlank(span.getAppLanguage())) {
      throw new InvalidSpanException("No application language", span);
    }

    if (isBlank(span.getAppPid())) {
      throw new InvalidSpanException("No application PID", span);
    }
  }

  private void validateOperation(EVSpan span) throws InvalidSpanException {
    /*
      By definition getOperationName().split("."):
        Last entry is method name,
        next to last is class name,
        remaining elements form the package name which must not be empty
    */
    String[] operationFqnSplit = span.getOperationName().split("\\.");
    if (operationFqnSplit.length < 3) {
      throw new InvalidSpanException("Invalid operation Name", span);
    }
  }

  private boolean isBlank(String s) {
    return s == null || s.isBlank();
  }

}
