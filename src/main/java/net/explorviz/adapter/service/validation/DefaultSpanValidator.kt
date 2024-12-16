package net.explorviz.adapter.service.validation;

import io.opentelemetry.proto.trace.v1.Span
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import net.explorviz.adapter.service.TokenService
import net.explorviz.adapter.service.converter.AttributesReader
import org.eclipse.microprofile.config.inject.ConfigProperty
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.DateTimeException

@ApplicationScoped
class DefaultSpanValidator @Inject constructor(
  private val tokenService: TokenService,
  @ConfigProperty(name = "explorviz.validate.token-existence")
  var validateTokens: Boolean = false
) : SpanValidator {

  companion object {
    private val LOGGER: Logger = LoggerFactory.getLogger(DefaultSpanValidator::class.java)
    private const val MIN_DEPTH_FQN_NAME = 3
  }

  override fun isValid(span: Span): Boolean {
    val attr = AttributesReader(span)

    return validateTimestamp(span.startTimeUnixNano) &&
        validateTimestamp(span.endTimeUnixNano) &&
        isValid(attr)
  }

  fun isValid(spanAttributes: AttributesReader): Boolean {
    return validateToken(spanAttributes.landscapeToken, spanAttributes.secret) &&
        validateHost(spanAttributes.hostName, spanAttributes.hostIpAddress) &&
        validateApp(spanAttributes.applicationName, spanAttributes.applicationLanguage) &&
        validateOperation(spanAttributes.methodFqn) &&
        validateK8s(spanAttributes)
  }

  private fun validateToken(token: String?, givenSecret: String?): Boolean {
    if (token.isNullOrBlank()) {
      LOGGER.trace("Invalid span: No or blank token.")
      return false
    }

    if (givenSecret.isNullOrBlank()) {
      LOGGER.trace("Invalid span: No or blank secret.")
      return false
    }

    if (!validateTokens) {
      return true
    }

    val validationResult = tokenService.validLandscapeTokenValueAndSecret(token, givenSecret!!)
    if (!validationResult) {
      LOGGER.trace("Invalid span: Token and/or secret are unknown.")
    }
    return validationResult
  }

  private fun validateTimestamp(timestamp: Long): Boolean {
    return try {
      if (timestamp <= 0L) {
        throw NumberFormatException("Time must be positive")
      }
      true
    } catch (e: DateTimeException) {
      LOGGER.trace("Invalid span: Contains invalid timestamp")
      false
    } catch (e: NumberFormatException) {
      LOGGER.trace("Invalid span: Contains invalid timestamp")
      false
    }
  }

  private fun validateHost(hostName: String?, hostIp: String?): Boolean {
    if (hostName.isNullOrBlank()) {
      LOGGER.trace("Invalid span: No hostname.")
      return false
    }

    if (hostIp.isNullOrBlank()) {
      LOGGER.trace("Invalid span: No IP address.")
      return false
    }

    return true
  }

  private fun validateApp(appName: String?, appLang: String?): Boolean {
    if (appName.isNullOrBlank()) {
      LOGGER.trace("Invalid span: No application name.")
      return false
    }

    if (appLang.isNullOrBlank()) {
      LOGGER.trace("Invalid span: No application language given.")
      return false
    }

    return true
  }

  private fun validateOperation(fqn: String): Boolean {
    val operationFqnSplit = fqn.split(".")
    if (operationFqnSplit.size < MIN_DEPTH_FQN_NAME) {
      LOGGER.trace("Invalid span: Invalid operation name: {}", fqn)
      return false
    }

    return operationFqnSplit[0].isNotBlank() &&
        operationFqnSplit[1].isNotBlank() &&
        operationFqnSplit[2].isNotBlank()
  }

  private fun validateK8s(spanAttributes: AttributesReader): Boolean {
    val hasPodName = spanAttributes.k8sPodName.isNotEmpty()
    val hasNamespace = spanAttributes.k8sNamespace.isNotEmpty()
    val hasNodeName = spanAttributes.k8sNodeName.isNotEmpty()
    val hasDeployment = spanAttributes.k8sDeploymentName.isNotEmpty()

    val hasAll = hasPodName && hasNamespace && hasNodeName && hasDeployment
    val hasNone = !hasPodName && !hasNamespace && !hasNodeName && !hasDeployment

    return hasAll || hasNone
  }
}
