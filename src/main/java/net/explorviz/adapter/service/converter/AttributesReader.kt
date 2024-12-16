package net.explorviz.adapter.service.converter

import io.opentelemetry.proto.common.v1.AnyValue
import io.opentelemetry.proto.trace.v1.Span
import net.explorviz.adapter.service.converter.DefaultAttributeValues.DEFAULT_APP_INSTANCE_ID
import net.explorviz.adapter.service.converter.DefaultAttributeValues.DEFAULT_APP_LANG
import net.explorviz.adapter.service.converter.DefaultAttributeValues.DEFAULT_APP_NAME
import net.explorviz.adapter.service.converter.DefaultAttributeValues.DEFAULT_CLASS_FQN
import net.explorviz.adapter.service.converter.DefaultAttributeValues.DEFAULT_FQN
import net.explorviz.adapter.service.converter.DefaultAttributeValues.DEFAULT_GIT_COMMIT_CHECKSUM
import net.explorviz.adapter.service.converter.DefaultAttributeValues.DEFAULT_HOST_IP
import net.explorviz.adapter.service.converter.DefaultAttributeValues.DEFAULT_HOST_NAME
import net.explorviz.adapter.service.converter.DefaultAttributeValues.DEFAULT_LANDSCAPE_SECRET
import net.explorviz.adapter.service.converter.DefaultAttributeValues.DEFAULT_LANDSCAPE_TOKEN
import net.explorviz.adapter.service.converter.DefaultAttributeValues.DEFAULT_PACKAGE_NAME
import org.apache.commons.lang3.StringUtils

/**
 * Reads the attributes of a [Span].
 */
open class AttributesReader(private val span: Span) {

  companion object {
    const val LANDSCAPE_TOKEN = "explorviz.token.id"
    const val TOKEN_SECRET = "explorviz.token.secret"
    const val GIT_COMMIT_CHECKSUM = "git_commit_checksum"
    const val HOST_NAME = "host"
    const val HOST_IP = "host_address"
    const val APPLICATION_NAME = "service.name"
    const val APPLICATION_INSTANCE_ID = "service.instance.id"
    const val APPLICATION_LANGUAGE = "telemetry.sdk.language"
    const val CODE_FUNCTION = "code.function"
    const val CODE_NAMESPACE = "code.namespace"
    const val METHOD_FQN = "java.fqn"
    const val K8S_POD_NAME = "k8s.pod.name"
    const val K8S_NAMESPACE_NAME = "k8s.namespace.name"
    const val K8S_NODE_NAME = "k8s.node.name"
    const val K8S_DEPLOYMENT_NAME = "k8s.deployment.name"
  }

  private val attributes: Map<String, AnyValue> =
    span.attributesList.associate { it.key to it.value }

  open fun getAsString(key: String): String? {
    return attributes[key]?.stringValue
  }

  val landscapeToken: String
    get() = getAsString(LANDSCAPE_TOKEN) ?: DEFAULT_LANDSCAPE_TOKEN

  val secret: String
    get() = getAsString(TOKEN_SECRET) ?: DEFAULT_LANDSCAPE_SECRET

  val hostName: String
    get() = getAsString(HOST_NAME) ?: DEFAULT_HOST_NAME

  val hostIpAddress: String
    get() = getAsString(HOST_IP) ?: DEFAULT_HOST_IP

  val gitCommitChecksum: String
    get() = getAsString(GIT_COMMIT_CHECKSUM) ?: DEFAULT_GIT_COMMIT_CHECKSUM

  val applicationName: String
    get() = getAsString(APPLICATION_NAME) ?: DEFAULT_APP_NAME

  val applicationInstanceId: String
    get() = getAsString(APPLICATION_INSTANCE_ID) ?: DEFAULT_APP_INSTANCE_ID

  val applicationLanguage: String
    get() = getAsString(APPLICATION_LANGUAGE) ?: DEFAULT_APP_LANG

  val methodFqn: String
    get() {
      val codeNamespace = getAsString(CODE_NAMESPACE)
      val codeFunction = getAsString(CODE_FUNCTION)
      val methodFqn = getAsString(METHOD_FQN)

      return codeNamespace?.let { namespace ->
        codeFunction?.let { function -> "$namespace.$function" }
      } ?: methodFqn ?: generateMethodFqnFromSpanName()
    }

  open fun generateMethodFqnFromSpanName(): String {
    val spanName = span.name
    if (spanName.isNullOrEmpty()) return DEFAULT_FQN

    val hierarchyDepth = StringUtils.countMatches(spanName, ".")

    return when {
      hierarchyDepth == 0 -> "$DEFAULT_CLASS_FQN.$spanName"
      hierarchyDepth == 1 -> "$DEFAULT_PACKAGE_NAME.$spanName"
      else -> spanName // Assume span name contains fully qualified name
    }
  }

  val k8sPodName: String
    get() = getAsString(K8S_POD_NAME) ?: ""

  val k8sNamespace: String
    get() = getAsString(K8S_NAMESPACE_NAME) ?: ""

  val k8sNodeName: String
    get() = getAsString(K8S_NODE_NAME) ?: ""

  val k8sDeploymentName: String
    get() = getAsString(K8S_DEPLOYMENT_NAME) ?: ""

  fun appendToSpan(builder: net.explorviz.avro.Span.Builder) {
    builder.apply {
      landscapeToken = this@AttributesReader.landscapeToken
      gitCommitChecksum = this@AttributesReader.gitCommitChecksum
      hostname = this@AttributesReader.hostName
      hostIpAddress = this@AttributesReader.hostIpAddress
      appInstanceId = this@AttributesReader.applicationInstanceId
      appName = this@AttributesReader.applicationName
      appLanguage = this@AttributesReader.applicationLanguage
      fullyQualifiedOperationName = this@AttributesReader.methodFqn
      k8sPodName = this@AttributesReader.k8sPodName
      k8sNamespace = this@AttributesReader.k8sNamespace
      k8sNodeName = this@AttributesReader.k8sNodeName
      k8sDeploymentName = this@AttributesReader.k8sDeploymentName
    }
  }
}
