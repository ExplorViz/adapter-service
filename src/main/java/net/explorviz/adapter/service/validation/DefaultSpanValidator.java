package net.explorviz.adapter.service.validation;

import io.opentelemetry.proto.trace.v1.Span;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.time.DateTimeException;
import net.explorviz.adapter.service.TokenService;
import net.explorviz.adapter.service.converter.AttributesReader;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Validator that checks that all values of the {@link Span} are set and valid.
 */
@ApplicationScoped
public class DefaultSpanValidator implements SpanValidator {

  private static final Logger LOGGER = LoggerFactory.getLogger(DefaultSpanValidator.class);

  private static final int MIN_DEPTH_FQN_NAME = 3;
  private final TokenService tokenService;
  @ConfigProperty(name = "explorviz.validate.token-existence")
  /* default */ boolean validateTokens; // NOCS

  @Inject
  public DefaultSpanValidator(final TokenService tokenService) {
    this.tokenService = tokenService;
  }

  @Override
  public boolean isValid(final Span span) {

    final AttributesReader attr = new AttributesReader(span);

    return this.validateTimestamp(span.getStartTimeUnixNano()) && this.validateTimestamp(
        span.getEndTimeUnixNano())
        && this.isValid(attr);
  }

  public boolean isValid(final AttributesReader spanAttributes) {
    return this.validateToken(spanAttributes.getLandscapeToken(), spanAttributes.getSecret())
        && this.validateHost(spanAttributes.getHostName(), spanAttributes.getHostIpAddress())
        && this.validateApp(spanAttributes.getApplicationName(),
        spanAttributes.getApplicationLanguage())
        && this.validateOperation(spanAttributes.getMethodFqn())
        && this.validateK8s(spanAttributes);
  }

  private boolean validateToken(final String token, final String givenSecret) {

    if (token == null || token.isBlank()) {
      if (LOGGER.isTraceEnabled()) {
        LOGGER.trace("Invalid span: No or blank token.");
      }
      return false;
    }

    if (givenSecret == null || givenSecret.isBlank()) {
      if (LOGGER.isTraceEnabled()) {
        LOGGER.trace("Invalid span: No or blank secret.");
      }
      return false;
    }

    if (!this.validateTokens) {
      return true;
    }

    final boolean validationResult = this.tokenService.validLandscapeTokenValueAndSecret(token,
        givenSecret);

    if (!validationResult && LOGGER.isTraceEnabled()) {
      LOGGER.trace("Invalid span: Token and/or secret are unknown.");
    }
    return validationResult;
  }

  private boolean validateTimestamp(final long timestamp) {
    try {
      if (timestamp <= 0L) { // NOPMD
        throw new NumberFormatException("Time must be positive");
      }
    } catch (DateTimeException | NumberFormatException e) {
      if (LOGGER.isTraceEnabled()) {
        LOGGER.trace("Invalid span: Contains invalid timestamp");
      }
      return false;
    }
    return true;
  }

  private boolean validateHost(final String hostName, final String hostIp) {
    if (this.isBlank(hostName)) {
      if (LOGGER.isTraceEnabled()) {
        LOGGER.trace("Invalid span: No hostname.");
      }
      return false;
    }
    if (this.isBlank(hostIp)) {
      if (LOGGER.isTraceEnabled()) {
        LOGGER.trace("Invalid span: No IP address.");
      }
      return false;
    }
    return true;
  }

  private boolean validateApp(final String appName, final String appLang) {
    if (this.isBlank(appName)) {
      if (LOGGER.isTraceEnabled()) {
        LOGGER.trace("Invalid span: No application name.");
      }
      return false;
    }

    if (this.isBlank(appLang)) {
      if (LOGGER.isTraceEnabled()) {
        LOGGER.trace("Invalid span: No application language given.");
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
      if (LOGGER.isTraceEnabled()) {
        LOGGER.trace("Invalid span: Invalid operation name: {}", fqn);
      }
      return false;
    }

    if (operationFqnSplit[0].isBlank()) {
      return false;
    }

    if (operationFqnSplit[1].isBlank()) {
      return false;
    }

    return !operationFqnSplit[2].isBlank();
  }

  private boolean isBlank(final String s) {
    return s == null || s.isBlank();
  }

  private boolean validateK8s(AttributesReader spanAttributes) {
    final var hasPodName = !spanAttributes.getK8sPodName().isEmpty();
    final var hasNamespace = !spanAttributes.getK8sNamespace().isEmpty();
    final var hasNodeName = !spanAttributes.getK8sNodeName().isEmpty();
    final var hasDeployment = !spanAttributes.getK8sDeploymentName().isEmpty();

    final var hasAll = hasPodName && hasNamespace && hasNodeName && hasDeployment;
    final var hasNone = !hasPodName && !hasNamespace && !hasNodeName && !hasDeployment;

    return hasAll || hasNone;
  }
}
