package net.explorviz.adapter.validation;


import java.time.DateTimeException;
import java.time.Instant;
import javax.enterprise.context.ApplicationScoped;
import net.explorviz.avro.SpanStructure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Validator that enforces that all values of the {@link SpanStructure} are set and valid.
 */
@ApplicationScoped
public class StrictValidator implements SpanValidator {

  private static final Logger LOGGER = LoggerFactory.getLogger(StrictValidator.class);

  @Override
  public boolean isValid(final SpanStructure span) {

    if (span.getLandscapeToken() == null || span.getLandscapeToken().isBlank()) {
      LOGGER.error("No token: {}", span);
      return false;
    }

    if (span.getHashCode() == null || span.getHashCode().isBlank()) {
      LOGGER.error("No hash code: {}", span);
      return false;
    }

    return this.validateTimestamp(span) &&
        this.validateHost(span) &&
        this.validateApp(span) &&
        this.validateOperation(span);
  }

  private boolean validateTimestamp(final SpanStructure span) {
    try {

      final Instant ignored = Instant.ofEpochSecond(span.getTimestamp().getSeconds(),
          span.getTimestamp().getNanoAdjust());

      if (ignored.getEpochSecond() <= 0) {
        throw new NumberFormatException("Time must be positive");
      }
    } catch (DateTimeException | NumberFormatException e) {
      LOGGER.error("Invalid timestamp: {}, {}", span, e);
      return false;
    }
    return true;
  }

  private boolean validateHost(final SpanStructure span) {
    if (this.isBlank(span.getHostname())) {
      LOGGER.error("No hostname: {}", span);
      return false;
    }
    if (this.isBlank(span.getHostIpAddress())) {
      LOGGER.error("No IP address: {}", span);
      return false;
    }
    return true;
  }

  private boolean validateApp(final SpanStructure span) {
    if (this.isBlank(span.getAppName())) {
      LOGGER.error("No application name: {}", span);
      return false;
    }

    if (this.isBlank(span.getAppLanguage())) {
      LOGGER.error("No application language: {}", span);
      return false;
    }

    if (this.isBlank(span.getAppPid())) {
      LOGGER.error("No application PID: {}", span);
      return false;
    }

    return true;
  }

  private boolean validateOperation(final SpanStructure span) {
    /*
     * By definition getFullyQualifiedOperationName().split("."): Last entry is method name, next to
     * last is class name, remaining elements form the package name which must not be empty
     */
    final String[] operationFqnSplit = span.getFullyQualifiedOperationName().split("\\.");
    if (operationFqnSplit.length < 3) {
      LOGGER.error("Invalid operation name: {}", span);
      return false;
    }
    return true;
  }

  private boolean isBlank(final String s) {
    return s == null || s.isBlank();
  }

}
