package net.explorviz.adapter.service.converter

import com.google.protobuf.ByteString
import io.opentelemetry.proto.common.v1.AnyValue
import io.opentelemetry.proto.common.v1.KeyValue
import io.opentelemetry.proto.trace.v1.Span
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class AttributesReaderTest {

    companion object {
        private const val KEY_LANDSCAPE_TOKEN = AttributesReader.LANDSCAPE_TOKEN
        private const val KEY_LANDSCAPE_SECRET = AttributesReader.TOKEN_SECRET
        private const val KEY_GIT_COMMIT_CHECKSUM = AttributesReader.GIT_COMMIT_CHECKSUM
        private const val KEY_HOST_NAME = AttributesReader.HOST_NAME
        private const val KEY_HOST_IP = AttributesReader.HOST_IP
        private const val KEY_APPLICATION_NAME = AttributesReader.APPLICATION_NAME
        private const val KEY_APPLICATION_INSTANCE_ID = AttributesReader.APPLICATION_INSTANCE_ID
        private const val KEY_APPLICATION_LANGUAGE = AttributesReader.APPLICATION_LANGUAGE
        private const val KEY_METHOD_FQN = AttributesReader.METHOD_FQN
        private const val KEY_K8S_POD_NAME = AttributesReader.K8S_POD_NAME
        private const val KEY_K8S_NAMESPACE = AttributesReader.K8S_NAMESPACE_NAME
        private const val KEY_K8S_DEPLOYMENT = AttributesReader.K8S_DEPLOYMENT_NAME
        private const val KEY_K8S_NODE = AttributesReader.K8S_NODE_NAME

        private const val TOKEN = "tok"
        private const val SECRET = "secret"
        private const val GIT_COMMIT_CHECKSUM = "gitCommitChecksum"
        private const val HOSTNAME = "Host"
        private const val HOST_IP = "1.2.3.4"
        private const val APP_NAME = "Test App"
        private const val APP_INSTANCE_ID = "1234L"
        private const val APP_LANG = "java"
        private const val FQN = "foo.bar.test()"
        private const val NAME = "span name"
        private const val K8S_POD_NAME = "my name is pod. james pod."
        private const val K8S_NAMESPACE = "name "
        private const val K8S_DEPLOYMENT = "deployment name"
        private const val K8S_NODE = "node.js"
    }

    @Test
    fun testValidSpanReadOut() {
        val attr = generateValidAttributesMap()
        val span = generateSpanFromAttributesMap(attr)
        val reader = AttributesReader(span)

        assertEquals(reader.landscapeToken, TOKEN)
        assertEquals(reader.secret, SECRET)
        assertEquals(reader.gitCommitChecksum, GIT_COMMIT_CHECKSUM)
        assertEquals(reader.hostName, HOSTNAME)
        assertEquals(reader.hostIpAddress, HOST_IP)
        assertEquals(reader.applicationName, APP_NAME)
        assertEquals(reader.applicationInstanceId, APP_INSTANCE_ID)
        assertEquals(reader.applicationLanguage, APP_LANG)
        assertEquals(reader.methodFqn, FQN)
        assertExcept(reader, null)
    }

    @Test
    fun testDefaultTokenReadOut() {
        var attr = generateValidAttributesMap()
        attr = removeElementAndReturnAttributesMap(KEY_LANDSCAPE_TOKEN, attr)
        val span = generateSpanFromAttributesMap(attr)
        val reader = AttributesReader(span)

        assertExcept(reader, AttributesReader.LANDSCAPE_TOKEN)
        assertEquals(reader.landscapeToken, DefaultAttributeValues.DEFAULT_LANDSCAPE_TOKEN)
    }

    @Test
    fun testDefaultSecretReadOut() {
        var attr = generateValidAttributesMap()
        attr = removeElementAndReturnAttributesMap(KEY_LANDSCAPE_SECRET, attr)
        val span = generateSpanFromAttributesMap(attr)
        val reader = AttributesReader(span)

        assertExcept(reader, AttributesReader.TOKEN_SECRET)
        assertEquals(reader.secret, DefaultAttributeValues.DEFAULT_LANDSCAPE_SECRET)
    }

    @Test
    fun testDefaultHostnameReadOut() {
        var attr = generateValidAttributesMap()
        attr = removeElementAndReturnAttributesMap(KEY_HOST_NAME, attr)
        val span = generateSpanFromAttributesMap(attr)
        val reader = AttributesReader(span)

        assertExcept(reader, AttributesReader.HOST_NAME)
        assertEquals(reader.hostName, DefaultAttributeValues.DEFAULT_HOST_NAME)
    }

    @Test
    fun testDefaultHostIpReadOut() {
        var attr = generateValidAttributesMap()
        attr = removeElementAndReturnAttributesMap(KEY_HOST_IP, attr)
        val span = generateSpanFromAttributesMap(attr)
        val reader = AttributesReader(span)

        assertExcept(reader, AttributesReader.HOST_IP)
        assertEquals(reader.hostIpAddress, DefaultAttributeValues.DEFAULT_HOST_IP)
    }

    @Test
    fun testDefaultAppNameReadOut() {
        var attr = generateValidAttributesMap()
        attr = removeElementAndReturnAttributesMap(KEY_APPLICATION_NAME, attr)
        val span = generateSpanFromAttributesMap(attr)
        val reader = AttributesReader(span)

        assertExcept(reader, AttributesReader.APPLICATION_NAME)
        assertEquals(reader.applicationName, DefaultAttributeValues.DEFAULT_APP_NAME)
    }

    @Test
    fun testDefaultAppInstanceIdReadOut() {
        var attr = generateValidAttributesMap()
        attr = removeElementAndReturnAttributesMap(KEY_APPLICATION_INSTANCE_ID, attr)
        val span = generateSpanFromAttributesMap(attr)
        val reader = AttributesReader(span)

        assertExcept(reader, AttributesReader.APPLICATION_INSTANCE_ID)
        assertEquals(reader.applicationInstanceId, DefaultAttributeValues.DEFAULT_APP_INSTANCE_ID)
    }

    @Test
    fun testDefaultAppLangReadOut() {
        var attr = generateValidAttributesMap()
        attr = removeElementAndReturnAttributesMap(KEY_APPLICATION_LANGUAGE, attr)
        val span = generateSpanFromAttributesMap(attr)
        val reader = AttributesReader(span)

        assertExcept(reader, AttributesReader.APPLICATION_LANGUAGE)
        assertEquals(reader.applicationLanguage, DefaultAttributeValues.DEFAULT_APP_LANG)
    }

    @Test
    fun testDefaultFqnReadOut() {
        var attr = generateValidAttributesMap()
        attr = removeElementAndReturnAttributesMap(KEY_METHOD_FQN, attr)
        val span = generateSpanFromAttributesMap(attr)
        val reader = AttributesReader(span)

        assertExcept(reader, AttributesReader.METHOD_FQN)
        assertEquals(reader.methodFqn, DefaultAttributeValues.DEFAULT_CLASS_FQN + "." + span.getName())
    }

    @Test
    fun testFqnFromNameReadOut() {
        val outerPackage = "net"
        val innerPackage = "explorviz"
        val className = "Reader"
        val methodName = "someNiceMethod()"

        // Only method name given
        var span = Span.newBuilder().setName(methodName).build()
        var reader = AttributesReader(span)
        assertEquals(DefaultAttributeValues.DEFAULT_CLASS_FQN + "." + methodName, reader.methodFqn)

        // Class and method given
        span = Span.newBuilder().setName("$className.$methodName").build()
        reader = AttributesReader(span)
        assertEquals("${DefaultAttributeValues.DEFAULT_PACKAGE_NAME}.${className}.${methodName}", reader.methodFqn)

        // Class and package given
        span = Span.newBuilder().setName("$innerPackage.$className.$methodName").build()
        reader = AttributesReader(span)
        assertEquals("$innerPackage.$className.$methodName", reader.methodFqn)

        // Class and two packages given
        span = Span.newBuilder().setName("$outerPackage.$innerPackage.$className.$methodName").build()
        reader = AttributesReader(span)
        assertEquals("$outerPackage.$innerPackage.$className.$methodName", reader.methodFqn)
    }

    private fun assertExcept(reader: AttributesReader, except: String?) {
        if (except != AttributesReader.LANDSCAPE_TOKEN) {
            assertEquals(reader.landscapeToken, TOKEN)
        }
        if (except != AttributesReader.TOKEN_SECRET) {
            assertEquals(reader.secret, SECRET)
        }
        if (except != AttributesReader.HOST_NAME) {
            assertEquals(reader.hostName, HOSTNAME)
        }
        if (except != AttributesReader.HOST_IP) {
            assertEquals(reader.hostIpAddress, HOST_IP)
        }
        if (except != AttributesReader.APPLICATION_NAME) {
            assertEquals(reader.applicationName, APP_NAME)
        }
        if (except != AttributesReader.APPLICATION_INSTANCE_ID) {
            assertEquals(reader.applicationInstanceId, APP_INSTANCE_ID)
        }
        if (except != AttributesReader.APPLICATION_LANGUAGE) {
            assertEquals(reader.applicationLanguage, APP_LANG)
        }
        if (except != AttributesReader.METHOD_FQN) {
            assertEquals(reader.methodFqn, FQN)
        }

        // Kubernetes-related checks
        if (except != AttributesReader.K8S_POD_NAME) {
            assertEquals(reader.k8sPodName, K8S_POD_NAME)
        }
        if (except != AttributesReader.K8S_NAMESPACE_NAME) {
            assertEquals(reader.k8sNamespace, K8S_NAMESPACE)
        }
        if (except != AttributesReader.K8S_DEPLOYMENT_NAME) {
            assertEquals(reader.k8sDeploymentName, K8S_DEPLOYMENT)
        }
        if (except != AttributesReader.K8S_NODE_NAME) {
            assertEquals(reader.k8sNodeName, K8S_NODE)
        }
    }

    private fun generateValidAttributesMap(): MutableList<KeyValue> {
        return mutableListOf(
            KeyValue.newBuilder()
                .setKey(KEY_LANDSCAPE_TOKEN)
                .setValue(AnyValue.newBuilder().setStringValue(TOKEN).build())
                .build(),
            KeyValue.newBuilder()
                .setKey(KEY_LANDSCAPE_SECRET)
                .setValue(AnyValue.newBuilder().setStringValue(SECRET).build())
                .build(),
            KeyValue.newBuilder()
                .setKey(KEY_GIT_COMMIT_CHECKSUM)
                .setValue(AnyValue.newBuilder().setStringValue(GIT_COMMIT_CHECKSUM).build())
                .build(),
            KeyValue.newBuilder()
                .setKey(KEY_HOST_NAME)
                .setValue(AnyValue.newBuilder().setStringValue(HOSTNAME).build())
                .build(),
            KeyValue.newBuilder()
                .setKey(KEY_HOST_IP)
                .setValue(AnyValue.newBuilder().setStringValue(HOST_IP).build())
                .build(),
            KeyValue.newBuilder()
                .setKey(KEY_APPLICATION_NAME)
                .setValue(AnyValue.newBuilder().setStringValue(APP_NAME).build())
                .build(),
            KeyValue.newBuilder()
                .setKey(KEY_APPLICATION_INSTANCE_ID)
                .setValue(AnyValue.newBuilder().setStringValue(APP_INSTANCE_ID).build())
                .build(),
            KeyValue.newBuilder()
                .setKey(KEY_APPLICATION_LANGUAGE)
                .setValue(AnyValue.newBuilder().setStringValue(APP_LANG).build())
                .build(),
            KeyValue.newBuilder()
                .setKey(KEY_METHOD_FQN)
                .setValue(AnyValue.newBuilder().setStringValue(FQN).build())
                .build(),
            KeyValue.newBuilder()
                .setKey(KEY_K8S_POD_NAME)
                .setValue(AnyValue.newBuilder().setStringValue(K8S_POD_NAME).build())
                .build(),
            KeyValue.newBuilder()
                .setKey(KEY_K8S_NAMESPACE)
                .setValue(AnyValue.newBuilder().setStringValue(K8S_NAMESPACE).build())
                .build(),
            KeyValue.newBuilder()
                .setKey(KEY_K8S_DEPLOYMENT)
                .setValue(AnyValue.newBuilder().setStringValue(K8S_DEPLOYMENT).build())
                .build(),
            KeyValue.newBuilder()
                .setKey(KEY_K8S_NODE)
                .setValue(AnyValue.newBuilder().setStringValue(K8S_NODE).build())
                .build()
        )
    }

    private fun removeElementAndReturnAttributesMap(key: String, map: MutableList<KeyValue>): MutableList<KeyValue> {
        return map.filter { it.key != key }.toMutableList()
    }

    private fun generateSpanFromAttributesMap(attributes: MutableList<KeyValue>): Span {
        return Span.newBuilder()
            .setTraceId(ByteString.copyFrom("50c246ad9c9883d1558df9f9b9ae7a6", Charsets.UTF_8))
            .setSpanId(ByteString.copyFrom("7ef83c66eabd5fbb", Charsets.UTF_8))
            .setParentSpanId(ByteString.copyFrom("7ef83c66efe42aaa", Charsets.UTF_8))
            .setName(NAME)
            .setStartTimeUnixNano(1667986986000L)
            .setEndTimeUnixNano(1667987046000L)
            .addAllAttributes(attributes)
            .build()
    }
}
