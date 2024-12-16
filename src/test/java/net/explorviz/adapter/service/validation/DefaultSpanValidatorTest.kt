package net.explorviz.adapter.service.validation;

import com.google.protobuf.ByteString
import io.opentelemetry.proto.common.v1.AnyValue
import io.opentelemetry.proto.common.v1.KeyValue
import io.opentelemetry.proto.trace.v1.Span
import net.explorviz.adapter.service.TokenService
import net.explorviz.adapter.service.converter.AttributesReader
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import java.nio.charset.Charset

class DefaultSpanValidatorTest {


  private val KEY_LANDSCAPE_TOKEN = AttributesReader.LANDSCAPE_TOKEN
  private val KEY_LANDSCAPE_SECRET = AttributesReader.TOKEN_SECRET
  private val KEY_HOST_NAME = AttributesReader.HOST_NAME
  private val KEY_HOST_IP = AttributesReader.HOST_IP
  private val KEY_APPLICATION_NAME = AttributesReader.APPLICATION_NAME
  private val KEY_APPLICATION_INSTANCE_ID = AttributesReader.APPLICATION_INSTANCE_ID
  private val KEY_APPLICATION_LANGUAGE = AttributesReader.APPLICATION_LANGUAGE
  private val KEY_METHOD_FQN = AttributesReader.METHOD_FQN

  private val TOKEN = "tok"
  private val SECRET = "secret"
  private val HOSTNAME = "Host"
  private val HOST_IP = "1.2.3.4"
  private val APP_NAME = "Test App"
  private val APP_INSTANCE_ID = "1234L"
  private val APP_LANG = "java"
  private val FQN = "foo.bar.test()"

  private lateinit var validator: DefaultSpanValidator
  private lateinit var validSpan: AttributesReader

  @BeforeEach
  fun setUp() {
    val mockTokenService = Mockito.mock(TokenService::class.java)
    Mockito.`when`(
      mockTokenService.validLandscapeTokenValueAndSecret(
        Mockito.anyString(),
        Mockito.anyString()
      )
    )
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
    Mockito.`when`(validSpan.methodFqn).thenReturn(FQN)
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
      newKeyValueString(KEY_METHOD_FQN, FQN)
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

  private fun removeElementAndReturnAttributesMap(
    keyToBeRemoved: String,
    attributes: List<KeyValue>
  ): List<KeyValue> {
    return attributes.filterNot { it.key == keyToBeRemoved }
  }

  private fun replaceElementAndReturnAttributesMap(
    key: String,
    newVal: String,
    attributes: List<KeyValue>
  ): List<KeyValue> {
    return attributes.map {
      if (it.key == key) newKeyValueString(key, newVal) else it
    }
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
      attrMap =
        replaceElementAndReturnAttributesMap(KEY_LANDSCAPE_TOKEN, invalidTokenValue, attrMap)
      val invalid = generateSpanFromAttributesMap(attrMap)
      assertFalse(validator.isValid(invalid))
    }
  }

  @Test
  fun invalidLandscapeTokenSecret() {
    for (invalidTokenSecret in listOf("", "\n", "\t", " ")) {
      var attrMap = generateValidAttributesMap()
      attrMap =
        replaceElementAndReturnAttributesMap(KEY_LANDSCAPE_SECRET, invalidTokenSecret, attrMap)
      val invalid = generateSpanFromAttributesMap(attrMap)
      assertFalse(validator.isValid(invalid))
    }
  }

  @Test
  fun testHost() {
    var attrMap = generateValidAttributesMap()

    attrMap = removeElementAndReturnAttributesMap(KEY_HOST_NAME, attrMap)
    var invalid = generateSpanFromAttributesMap(attrMap)
    assertTrue(validator.isValid(invalid))

    attrMap = generateValidAttributesMap()
    attrMap = removeElementAndReturnAttributesMap(KEY_HOST_IP, attrMap)
    invalid = generateSpanFromAttributesMap(attrMap)
    assertTrue(validator.isValid(invalid))

    attrMap = generateValidAttributesMap()
    attrMap = replaceElementAndReturnAttributesMap(KEY_HOST_IP, " ", attrMap)
    invalid = generateSpanFromAttributesMap(attrMap)
    assertFalse(validator.isValid(invalid))

    for (invalidHostName in listOf("", "\n", "\t", " ")) {
      for (invalidHostIp in listOf("", "\t", "\n", " ")) {
        attrMap = generateValidAttributesMap()
        attrMap = replaceElementAndReturnAttributesMap(KEY_HOST_NAME, invalidHostName, attrMap)
        attrMap = replaceElementAndReturnAttributesMap(KEY_HOST_IP, invalidHostIp, attrMap)
        invalid = generateSpanFromAttributesMap(attrMap)
        assertFalse(validator.isValid(invalid))
      }
    }
  }

  @Test
  fun testApp() {
    var attrMap = generateValidAttributesMap()

    attrMap = removeElementAndReturnAttributesMap(KEY_APPLICATION_NAME, attrMap)
    var invalid = generateSpanFromAttributesMap(attrMap)
    assertTrue(validator.isValid(invalid))

    attrMap = generateValidAttributesMap()
    attrMap = removeElementAndReturnAttributesMap(KEY_APPLICATION_LANGUAGE, attrMap)
    invalid = generateSpanFromAttributesMap(attrMap)
    assertTrue(validator.isValid(invalid))

    attrMap = generateValidAttributesMap()
    attrMap = replaceElementAndReturnAttributesMap(KEY_APPLICATION_NAME, " ", attrMap)
    invalid = generateSpanFromAttributesMap(attrMap)
    assertFalse(validator.isValid(invalid))

    attrMap = generateValidAttributesMap()
    attrMap = replaceElementAndReturnAttributesMap(KEY_APPLICATION_LANGUAGE, " ", attrMap)
    invalid = generateSpanFromAttributesMap(attrMap)
    assertFalse(validator.isValid(invalid))

    for (invalidId in listOf("", "\n", "\t", " ")) {
      for (invalidLanguage in listOf("", "\t", "\n", " ")) {
        attrMap = generateValidAttributesMap()
        attrMap = replaceElementAndReturnAttributesMap(KEY_APPLICATION_NAME, invalidId, attrMap)
        attrMap =
          replaceElementAndReturnAttributesMap(KEY_APPLICATION_LANGUAGE, invalidLanguage, attrMap)
        invalid = generateSpanFromAttributesMap(attrMap)
        assertFalse(validator.isValid(invalid))
      }
    }
  }

  @Test
  fun testOperation() {
    var attrMap = generateValidAttributesMap()

    val invalidValues = listOf(
      "",
      "\n",
      "\t",
      " ",
      "noMethod",
      "classNoPackage.method",
      "...",
      "a..",
      "a.b.",
      "a.b. ",
      "a..c",
      ".b.c",
      "..c",
      ".b."
    )
    for (invalidMethodFqn in invalidValues) {
      attrMap = generateValidAttributesMap()
      attrMap = replaceElementAndReturnAttributesMap(KEY_METHOD_FQN, invalidMethodFqn, attrMap)
      val invalid = generateSpanFromAttributesMap(attrMap)
      assertFalse(validator.isValid(invalid))
    }
  }

  @Test
  fun testTimestamps() {
    val attrMap = generateValidAttributesMap()

    val invalids = listOf(
      Span.newBuilder()
        .setTraceId(
          ByteString.copyFrom(
            "50c246ad9c9883d1558df9f19b9ae7a6",
            Charset.defaultCharset()
          )
        )
        .setSpanId(ByteString.copyFrom("7ef83c66eabd5fbb", Charset.defaultCharset()))
        .setParentSpanId(ByteString.copyFrom("7ef83c66efe42aaa", Charset.defaultCharset()))
        .setStartTimeUnixNano(0L)
        .setEndTimeUnixNano(456L)
        .addAllAttributes(attrMap)
        .build(),
      Span.newBuilder()
        .setTraceId(
          ByteString.copyFrom(
            "50c246ad9c9883d1558df9f19b9ae7a6",
            Charset.defaultCharset()
          )
        )
        .setSpanId(ByteString.copyFrom("7ef83c66eabd5fbb", Charset.defaultCharset()))
        .setParentSpanId(ByteString.copyFrom("7ef83c66efe42aaa", Charset.defaultCharset()))
        .setStartTimeUnixNano(456L)
        .setEndTimeUnixNano(0L)
        .addAllAttributes(attrMap)
        .build(),
      Span.newBuilder()
        .setTraceId(
          ByteString.copyFrom(
            "50c246ad9c9883d1558df9f19b9ae7a6",
            Charset.defaultCharset()
          )
        )
        .setSpanId(ByteString.copyFrom("7ef83c66eabd5fbb", Charset.defaultCharset()))
        .setParentSpanId(ByteString.copyFrom("7ef83c66efe42aaa", Charset.defaultCharset()))
        .setStartTimeUnixNano(0L)
        .setEndTimeUnixNano(0L)
        .addAllAttributes(attrMap)
        .build()
    )

    for (invalid in invalids) {
      assertFalse(validator.isValid(invalid))
    }
  }

  companion object {
    fun newKeyValueString(key: String, value: String): KeyValue {
      return KeyValue.newBuilder()
        .setKey(key)
        .setValue(AnyValue.newBuilder().setStringValue(value).build())
        .build()
    }
  }
}

