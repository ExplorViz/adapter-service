package net.explorviz.adapter.service.validation;

import io.quarkus.runtime.StartupEvent;
import java.time.DateTimeException;
import java.time.Instant;
import javax.annotation.Priority;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import net.explorviz.adapter.service.TokenService;
import net.explorviz.avro.SpanStructure;
import org.apache.kafka.streams.KafkaStreams;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Validator that enforces that all values of the {@link SpanStructure} are set and valid.
 */
@ApplicationScoped
public class StrictValidator implements SpanValidator {

  private static final Logger LOGGER = LoggerFactory.getLogger(StrictValidator.class);

  private static final int MIN_DEPTH_FQN_NAME = 3;

  private final TokenService tokenService;

  @ConfigProperty(name = "explorviz.validate.token-existence")
  boolean validateTokens;

  @Inject
  public StrictValidator(final TokenService tokenService) {
    this.tokenService = tokenService;
  }

  @Override
  public boolean isValid(final SpanStructure span) {

    if (span.getHashCode() == null || span.getHashCode().isBlank()) {
      if (LOGGER.isErrorEnabled()) {
        LOGGER.error("No hash code: {}", span);
      }
      return false;
    }

    return this.validateToken(span.getLandscapeToken()) && this.validateTimestamp(span)
        && this.validateHost(span)
        && this.validateApp(span)
        && this.validateOperation(span);
  }

  private boolean validateToken(final String token) {
    if (token == null || token.isBlank()) {
      return false;
    }

    // validateTokens -> tokenExists
    boolean exists = !validateTokens | this.tokenService.exists(token) ;

    return exists;
  }

  private boolean validateTimestamp(final SpanStructure span) {
    try {

      final Instant ignored = Instant.ofEpochSecond(span.getTimestamp().getSeconds(),
          span.getTimestamp().getNanoAdjust());

      if (ignored.getEpochSecond() <= 0) {
        throw new NumberFormatException("Time must be positive");
      }
    } catch (DateTimeException | NumberFormatException e) {
      if (LOGGER.isErrorEnabled()) {
        LOGGER.error("Invalid timestamp: {}, {}", span, e);
      }
      return false;
    }
    return true;
  }

  private boolean validateHost(final SpanStructure span) {
    if (this.isBlank(span.getHostname())) {
      if (LOGGER.isErrorEnabled()) {
        LOGGER.error("No hostname: {}", span);
      }
      return false;
    }
    if (this.isBlank(span.getHostIpAddress())) {
      if (LOGGER.isErrorEnabled()) {
        LOGGER.error("No IP address: {}", span);
      }
      return false;
    }
    return true;
  }

  private boolean validateApp(final SpanStructure span) {
    if (this.isBlank(span.getAppName())) {
      if (LOGGER.isErrorEnabled()) {
        LOGGER.error("No application name: {}", span);
      }
      return false;
    }

    if (this.isBlank(span.getAppLanguage())) {
      if (LOGGER.isErrorEnabled()) {
        LOGGER.error("No application language: {}", span);
      }
      return false;
    }

    if (this.isBlank(span.getAppPid())) {
      if (LOGGER.isErrorEnabled()) {
        LOGGER.error("No application PID: {}", span);
      }
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
    if (operationFqnSplit.length < MIN_DEPTH_FQN_NAME) {
      if (LOGGER.isErrorEnabled()) {
        LOGGER.error("Invalid operation name: {}", span);
      }
      return false;
    }
    return true;
  }

  private boolean isBlank(final String s) {
    return s == null || s.isBlank();
  }

}
