package net.explorviz.adapter.service.converter

import io.opentelemetry.proto.common.v1.AnyValue
import io.opentelemetry.proto.trace.v1.Span
import net.explorviz.adapter.service.converter.DefaultAttributeValues.DEFAULT_APP_NAME
import net.explorviz.adapter.service.converter.DefaultAttributeValues.DEFAULT_FUNCTION_NAME
import net.explorviz.adapter.service.converter.DefaultAttributeValues.DEFAULT_LANDSCAPE_SECRET
import net.explorviz.adapter.service.converter.DefaultAttributeValues.DEFAULT_LANDSCAPE_TOKEN
import net.explorviz.adapter.service.converter.DefaultAttributeValues.DEFAULT_NAMESPACE
import net.explorviz.adapter.service.converter.DefaultAttributeValues.DEFAULT_PACKAGE_NAME

/** Reads the attributes of a [Span]. */
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

    private val attributes: Map<String, AnyValue> = span.attributesList.associate { it.key to it.value }

    open fun getAsString(key: String): String? {
        return attributes[key]?.stringValue
    }

    val landscapeToken: String
        get() = getAsString(LANDSCAPE_TOKEN) ?: DEFAULT_LANDSCAPE_TOKEN

    val secret: String
        get() = getAsString(TOKEN_SECRET) ?: DEFAULT_LANDSCAPE_SECRET

    val hostName: String?
        get() = getAsString(HOST_NAME)

    val hostIpAddress: String?
        get() = getAsString(HOST_IP)

    val gitCommitChecksum: String?
        get() = getAsString(GIT_COMMIT_CHECKSUM)

    val applicationName: String
        get() = getAsString(APPLICATION_NAME) ?: DEFAULT_APP_NAME

    val applicationInstanceId: String?
        get() = getAsString(APPLICATION_INSTANCE_ID)

    val applicationLanguage: String?
        get() = getAsString(APPLICATION_LANGUAGE)

    val namespace: String
        get() {
            return getAsString(CODE_NAMESPACE) ?: generateNamespaceFromSpanName()
        }

    val functionName: String
        get() {
            return getAsString(CODE_FUNCTION) ?: generateFunctionNameFromSpanName()
        }

    open fun generateNamespaceFromSpanName(): String {
        val spanName = span.name
        if (spanName.isNullOrEmpty()) return DEFAULT_NAMESPACE

        val fqnComponents = spanName.split(".")

        val namespaceInSpanName =
            spanName.substring(
                0,
                Math.max(0, spanName.length - (fqnComponents.last().length + 1)),
            )

        return when (fqnComponents.size) {
            1 -> DEFAULT_NAMESPACE
            2 -> "${DEFAULT_PACKAGE_NAME}.${namespaceInSpanName}"
            else -> namespaceInSpanName
        }
    }

    open fun generateFunctionNameFromSpanName(): String {
        val spanName = span.name
        if (spanName.isNullOrEmpty()) return DEFAULT_FUNCTION_NAME

        val fqnComponents = spanName.split(".")

        return if (fqnComponents.size <= 1) {
            spanName
        } else {
            spanName.substring(
                spanName.length - fqnComponents.last().length,
                spanName.length,
            )
        }
    }

    val k8sPodName: String?
        get() = getAsString(K8S_POD_NAME)

    val k8sNamespace: String?
        get() = getAsString(K8S_NAMESPACE_NAME)

    val k8sNodeName: String?
        get() = getAsString(K8S_NODE_NAME)

    val k8sDeploymentName: String?
        get() = getAsString(K8S_DEPLOYMENT_NAME)

    fun appendToSpan(builder: net.explorviz.avro.Span.Builder) {
        builder.apply {
            landscapeToken = this@AttributesReader.landscapeToken
            appName = this@AttributesReader.applicationName
            namespace = this@AttributesReader.namespace
            functionName = this@AttributesReader.functionName
            gitCommitChecksum = this@AttributesReader.gitCommitChecksum
            hostname = this@AttributesReader.hostName
            hostIpAddress = this@AttributesReader.hostIpAddress
            appInstanceId = this@AttributesReader.applicationInstanceId
            appLanguage = this@AttributesReader.applicationLanguage
            k8sPodName = this@AttributesReader.k8sPodName
            k8sNamespace = this@AttributesReader.k8sNamespace
            k8sNodeName = this@AttributesReader.k8sNodeName
            k8sDeploymentName = this@AttributesReader.k8sDeploymentName
        }
    }
}
