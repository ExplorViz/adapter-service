package net.explorviz.adapter.service.validation

import com.google.protobuf.ByteString
import io.opentelemetry.proto.common.v1.AnyValue
import io.opentelemetry.proto.common.v1.KeyValue
import io.opentelemetry.proto.trace.v1.Span
import java.nio.charset.Charset
import net.explorviz.adapter.service.TokenService
import net.explorviz.adapter.service.converter.AttributesReader
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito

class DefaultSpanValidatorTest {

    private companion object {
        const val KEY_LANDSCAPE_TOKEN = AttributesReader.LANDSCAPE_TOKEN
        const val KEY_LANDSCAPE_SECRET = AttributesReader.TOKEN_SECRET
        const val KEY_HOST_NAME = AttributesReader.HOST_NAME
        const val KEY_HOST_IP = AttributesReader.HOST_IP
        const val KEY_APPLICATION_NAME = AttributesReader.APPLICATION_NAME
        const val KEY_APPLICATION_INSTANCE_ID = AttributesReader.APPLICATION_INSTANCE_ID
        const val KEY_APPLICATION_LANGUAGE = AttributesReader.APPLICATION_LANGUAGE

        const val TOKEN = "tok"
        const val SECRET = "secret"
        const val HOSTNAME = "Host"
        const val HOST_IP = "1.2.3.4"
        const val APP_NAME = "Test App"
        const val APP_INSTANCE_ID = "1234L"
        const val APP_LANG = "java"

        fun newKeyValueString(key: String, value: String): KeyValue {
            return KeyValue.newBuilder()
                .setKey(key)
                .setValue(AnyValue.newBuilder().setStringValue(value).build())
                .build()
        }
    }

    private lateinit var validator: DefaultSpanValidator
    private lateinit var validSpan: AttributesReader

    @BeforeEach
    fun setUp() {
        val mockTokenService = Mockito.mock(TokenService::class.java)
        Mockito.`when`(mockTokenService.validLandscapeTokenValueAndSecret(Mockito.anyString(), Mockito.anyString()))
            .thenReturn(true)

        validator = DefaultSpanValidator(mockTokenService, true)

        validSpan = Mockito.mock(AttributesReader::class.java)
        Mockito.`when`(validSpan.secret).thenReturn(SECRET)
        Mockito.`when`(validSpan.landscapeToken).thenReturn(TOKEN)
        Mockito.`when`(validSpan.hostName).thenReturn(HOSTNAME)
        Mockito.`when`(validSpan.hostIpAddress).thenReturn(HOST_IP)
        Mockito.`when`(validSpan.applicationName).thenReturn(APP_NAME)
        Mockito.`when`(validSpan.applicationInstanceId).thenReturn(APP_INSTANCE_ID)
        Mockito.`when`(validSpan.applicationLanguage).thenReturn(APP_LANG)
    }

    private fun generateValidAttributesMap(): List<KeyValue> {
        return listOf(
            newKeyValueString(KEY_LANDSCAPE_TOKEN, TOKEN),
            newKeyValueString(KEY_LANDSCAPE_SECRET, SECRET),
            newKeyValueString(KEY_HOST_NAME, HOSTNAME),
            newKeyValueString(KEY_HOST_IP, HOST_IP),
            newKeyValueString(KEY_APPLICATION_NAME, APP_NAME),
            newKeyValueString(KEY_APPLICATION_INSTANCE_ID, APP_INSTANCE_ID),
            newKeyValueString(KEY_APPLICATION_LANGUAGE, APP_LANG),
        )
    }

    private fun generateSpanFromAttributesMap(attributes: List<KeyValue>): Span {
        return Span.newBuilder()
            .setTraceId(ByteString.copyFrom("50c246ad9c9883d1558df9f19b9ae7a6", Charset.defaultCharset()))
            .setSpanId(ByteString.copyFrom("7ef83c66eabd5fbb", Charset.defaultCharset()))
            .setParentSpanId(ByteString.copyFrom("7ef83c66efe42aaa", Charset.defaultCharset()))
            .setStartTimeUnixNano(1667986986000L)
            .setEndTimeUnixNano(1667987046000L)
            .addAllAttributes(attributes)
            .build()
    }

    private fun replaceElementAndReturnAttributesMap(
        key: String,
        newVal: String,
        attributes: List<KeyValue>
    ): List<KeyValue> {
        return attributes.map { if (it.key == key) newKeyValueString(key, newVal) else it }
    }

    @Test
    fun valid() {
        val attrMap = generateValidAttributesMap()
        val valid = generateSpanFromAttributesMap(attrMap)
        assertTrue(validator.isValid(valid))
    }

    @Test
    fun invalidLandscapeTokenValue() {
        for (invalidTokenValue in listOf("", "\n", "\t", " ")) {
            var attrMap = generateValidAttributesMap()
            attrMap = replaceElementAndReturnAttributesMap(KEY_LANDSCAPE_TOKEN, invalidTokenValue, attrMap)
            val invalid = generateSpanFromAttributesMap(attrMap)
            assertFalse(validator.isValid(invalid))
        }
    }

    @Test
    fun invalidLandscapeTokenSecret() {
        for (invalidTokenSecret in listOf("", "\n", "\t", " ")) {
            var attrMap = generateValidAttributesMap()
            attrMap = replaceElementAndReturnAttributesMap(KEY_LANDSCAPE_SECRET, invalidTokenSecret, attrMap)
            val invalid = generateSpanFromAttributesMap(attrMap)
            assertFalse(validator.isValid(invalid))
        }
    }

    @Test
    fun testTimestamps() {
        val attrMap = generateValidAttributesMap()

        val invalids =
            listOf(
                Span.newBuilder()
                    .setTraceId(ByteString.copyFrom("50c246ad9c9883d1558df9f19b9ae7a6", Charset.defaultCharset()))
                    .setSpanId(ByteString.copyFrom("7ef83c66eabd5fbb", Charset.defaultCharset()))
                    .setParentSpanId(ByteString.copyFrom("7ef83c66efe42aaa", Charset.defaultCharset()))
                    .setStartTimeUnixNano(0L)
                    .setEndTimeUnixNano(456L)
                    .addAllAttributes(attrMap)
                    .build(),
                Span.newBuilder()
                    .setTraceId(ByteString.copyFrom("50c246ad9c9883d1558df9f19b9ae7a6", Charset.defaultCharset()))
                    .setSpanId(ByteString.copyFrom("7ef83c66eabd5fbb", Charset.defaultCharset()))
                    .setParentSpanId(ByteString.copyFrom("7ef83c66efe42aaa", Charset.defaultCharset()))
                    .setStartTimeUnixNano(456L)
                    .setEndTimeUnixNano(0L)
                    .addAllAttributes(attrMap)
                    .build(),
                Span.newBuilder()
                    .setTraceId(ByteString.copyFrom("50c246ad9c9883d1558df9f19b9ae7a6", Charset.defaultCharset()))
                    .setSpanId(ByteString.copyFrom("7ef83c66eabd5fbb", Charset.defaultCharset()))
                    .setParentSpanId(ByteString.copyFrom("7ef83c66efe42aaa", Charset.defaultCharset()))
                    .setStartTimeUnixNano(0L)
                    .setEndTimeUnixNano(0L)
                    .addAllAttributes(attrMap)
                    .build(),
            )

        for (invalid in invalids) {
            assertFalse(validator.isValid(invalid))
        }
    }
}
