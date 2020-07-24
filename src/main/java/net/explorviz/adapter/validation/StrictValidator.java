package net.explorviz.adapter.validation;


import java.time.DateTimeException;
import java.time.Instant;
import javax.enterprise.context.ApplicationScoped;
import net.explorviz.avro.EVSpan;

/**
 * Validator that enforces that all values of the {@link EVSpan} are set and valid. Does not
 * sanitize invalid attributes with default values.
 */
@ApplicationScoped
public class StrictValidator implements SpanValidator {

  @Override
  public void validate(final EVSpan span) throws InvalidSpanException {

    if (span.getLandscapeToken() == null || span.getLandscapeToken().isBlank()) {
      throw new InvalidSpanException("No token", span);
    }

    if (span.getHashCode() == null || span.getHashCode().isBlank()) {
      throw new InvalidSpanException("No hash code", span);
    }

    this.validateTimestamp(span);
    this.validateHost(span);
    this.validateApp(span);
    this.validateOperation(span);
  }

  @Override
  public boolean isValid(final EVSpan span) {
    try {
      this.validate(span);
      return true;
    } catch (final InvalidSpanException e) {
      return false;
    }
  }

  private void validateTimestamp(final EVSpan span) throws InvalidSpanException {
    try {
      final Instant ignored = Instant.ofEpochSecond(span.getTimestamp().getSeconds(),
          span.getTimestamp().getNanoAdjust());
      if (ignored.getEpochSecond() <= 0) {
        throw new NumberFormatException("Time must be positive");
      }
    } catch (DateTimeException | NumberFormatException e) {
      throw new InvalidSpanException("Invalid timestamp", e, span);
    }
  }

  private void validateHost(final EVSpan span) throws InvalidSpanException {
    if (this.isBlank(span.getHostname())) {
      throw new InvalidSpanException("No hostname", span);
    }
    if (this.isBlank(span.getHostIpAddress())) {
      throw new InvalidSpanException("No IP address", span);
    }
  }

  private void validateApp(final EVSpan span) throws InvalidSpanException {
    if (this.isBlank(span.getAppName())) {
      throw new InvalidSpanException("No application name", span);
    }

    if (this.isBlank(span.getAppLanguage())) {
      throw new InvalidSpanException("No application language", span);
    }

    if (this.isBlank(span.getAppPid())) {
      throw new InvalidSpanException("No application PID", span);
    }
  }

  private void validateOperation(final EVSpan span) throws InvalidSpanException {
    /*
     * By definition getFullyQualifiedOperationName().split("."): Last entry is method name, next to
     * last is class name, remaining elements form the package name which must not be empty
     */
    final String[] operationFqnSplit = span.getFullyQualifiedOperationName().split("\\.");
    if (operationFqnSplit.length < 3) {
      throw new InvalidSpanException("Invalid operation Name", span);
    }
  }

  private boolean isBlank(final String s) {
    return s == null || s.isBlank();
  }

}
