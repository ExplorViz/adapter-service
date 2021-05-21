package net.explorviz.adapter.service.validation;

import com.google.protobuf.Timestamp;
import io.opencensus.proto.trace.v1.Span;
import java.time.DateTimeException;
import java.time.Instant;
import java.util.Optional;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import net.explorviz.adapter.service.TokenService;
import net.explorviz.adapter.service.converter.AttributesReader;
import net.explorviz.avro.SpanStructure;
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

  @SuppressWarnings("PMD.DefaultPackage")
  @ConfigProperty(name = "explorviz.validate.token-existence")
  /* default */ boolean validateTokens; // NOCS

  private final TokenService tokenService;

  @Inject
  public StrictValidator(final TokenService tokenService) {
    this.tokenService = tokenService;
  }

  @Override
  public boolean isValid(final Span span) {

    final AttributesReader attr = new AttributesReader(span);

    return this.validateTimestamp(span.getStartTime())
        && this.validateTimestamp(span.getEndTime())
        && this.isValid(attr);

  }

  public boolean isValid(final AttributesReader spanAttributes) {
    return this.validateToken(spanAttributes.getLandscapeToken(), spanAttributes.getSecret())
        && this.validateHost(spanAttributes.getHostName(), spanAttributes.getHostIpAddress())
        && this.validateApp(spanAttributes.getApplicationName(),
            spanAttributes.getApplicationLanguage())
        && this.validateOperation(spanAttributes.getMethodFqn());
  }

  private boolean validateToken(final String token, final String givenSecret) {

    if (token == null || token.isBlank()) {
      if (LOGGER.isTraceEnabled()) {
        LOGGER.trace("Discarded span with no token");
      }
      return false;
    }

    if (givenSecret == null || givenSecret.isBlank()) {
      if (LOGGER.isTraceEnabled()) {
        LOGGER.trace("Discarded span with no secret");
      }
      return false;
    }

    if (!this.validateTokens) {
      if (LOGGER.isTraceEnabled()) {
        LOGGER.trace("Discarded validation of token and secret. Property is set.");
      }
      return true;
    }

    final Optional<String> secretOptional = this.tokenService.getSecret(token);
    if (secretOptional.isEmpty()) {
      if (LOGGER.isTraceEnabled()) {
        LOGGER.trace("Discarded span with unknown token");
      }
      return false;
    } else {
      final String secret = secretOptional.get();
      if (!secret.equals(givenSecret)) {
        if (LOGGER.isWarnEnabled()) {
          LOGGER.warn("Discarded span with invalid secret");
        }
        return false;
      }
    }

    return true;
  }

  private boolean validateTimestamp(final Timestamp timestamp) {
    try {

      final Instant ignored = Instant.ofEpochSecond(timestamp.getSeconds(),
          timestamp.getNanos());

      if (ignored.getEpochSecond() <= 0) {
        throw new NumberFormatException("Time must be positive");
      }
    } catch (DateTimeException | NumberFormatException e) {
      if (LOGGER.isErrorEnabled()) {
        LOGGER.error("Invalid timestamp");
      }
      return false;
    }
    return true;
  }

  private boolean validateHost(final String hostName, final String hostIp) {
    if (this.isBlank(hostName)) {
      if (LOGGER.isErrorEnabled()) {
        LOGGER.error("No hostname given");
      }
      return false;
    }
    if (this.isBlank(hostIp)) {
      if (LOGGER.isErrorEnabled()) {
        LOGGER.error("No IP address given");
      }
      return false;
    }
    return true;
  }

  private boolean validateApp(final String appName, final String appLang) {
    if (this.isBlank(appName)) {
      if (LOGGER.isErrorEnabled()) {
        LOGGER.error("No application name given");
      }
      return false;
    }

    if (this.isBlank(appLang)) {
      if (LOGGER.isErrorEnabled()) {
        LOGGER.error("No application language given");
      }
      return false;
    }

    return true;
  }

  private boolean validateOperation(final String fqn) {
    /*
     * By definition getFullyQualifiedOperationName().split("."): Last entry is method name, next to
     * last is class name, remaining elements form the package name which must not be empty
     */
    final String[] operationFqnSplit = fqn.split("\\.");
    if (operationFqnSplit.length < MIN_DEPTH_FQN_NAME) {
      if (LOGGER.isErrorEnabled()) {
        LOGGER.error("Invalid operation name: {}", fqn);
      }
      return false;
    }
    return true;
  }

  private boolean isBlank(final String s) {
    return s == null || s.isBlank();
  }

}
