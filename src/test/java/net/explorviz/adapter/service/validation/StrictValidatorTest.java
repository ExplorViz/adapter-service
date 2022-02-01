package net.explorviz.adapter.service.validation;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import com.google.protobuf.ByteString;
import com.google.protobuf.Timestamp;
import io.opencensus.proto.trace.v1.AttributeValue;
import io.opencensus.proto.trace.v1.Span;
import io.opencensus.proto.trace.v1.TruncatableString;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import net.explorviz.adapter.service.TokenService;
import net.explorviz.adapter.service.converter.AttributesReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;

class StrictValidatorTest {

  private StrictValidator validator;
  private AttributesReader validSpan;

  @BeforeEach
  void setUp() {
    final TokenService mockTokenService = Mockito.mock(TokenService.class);
    Mockito.when(mockTokenService.validLandscapeTokenValueAndSecret(Matchers.anyString(),
        Matchers.anyString())).thenReturn(true);
    this.validator = new StrictValidator(mockTokenService);

    final String token = "tok";
    final String secret = "secret";
    final String hostname = "Host";
    final String hostIp = "1.2.3.4";
    final String appName = "Test App";
    final String appInstanceId = "1234L";
    final String appLang = "java";
    final String fqn = "foo.bar.test()";

    this.validSpan = Mockito.mock(AttributesReader.class);
    Mockito.when(this.validSpan.getSecret()).thenReturn(secret);
    Mockito.when(this.validSpan.getLandscapeToken()).thenReturn(token);
    Mockito.when(this.validSpan.getHostName()).thenReturn(hostname);
    Mockito.when(this.validSpan.getHostIpAddress()).thenReturn(hostIp);
    Mockito.when(this.validSpan.getApplicationName()).thenReturn(appName);
    Mockito.when(this.validSpan.getApplicationInstanceId()).thenReturn(appInstanceId);
    Mockito.when(this.validSpan.getApplicationLanguage()).thenReturn(appLang);
    Mockito.when(this.validSpan.getMethodFqn()).thenReturn(fqn);

  }

  private Map<String, AttributeValue> generateValidAttributesMap() {
    final Map<String, AttributeValue> attrMap = new HashMap<>();
    attrMap.put(AttributesReader.LANDSCAPE_TOKEN, AttributeValue.newBuilder()
        .setStringValue(TruncatableString.newBuilder().setValue("token")).build());
    attrMap.put(AttributesReader.TOKEN_SECRET, AttributeValue.newBuilder()
        .setStringValue(TruncatableString.newBuilder().setValue("secret")).build());
    attrMap.put(AttributesReader.HOST_IP, AttributeValue.newBuilder()
        .setStringValue(TruncatableString.newBuilder().setValue("1.2.3.4")).build());
    attrMap.put(AttributesReader.HOST_NAME, AttributeValue.newBuilder()
        .setStringValue(TruncatableString.newBuilder().setValue("hostname")).build());
    attrMap.put(AttributesReader.APPLICATION_LANGUAGE, AttributeValue.newBuilder()
        .setStringValue(TruncatableString.newBuilder().setValue("language")).build());
    attrMap.put(AttributesReader.APPLICATION_NAME, AttributeValue.newBuilder()
        .setStringValue(TruncatableString.newBuilder().setValue("appname")).build());
    attrMap.put(AttributesReader.APPLICATION_INSTANCE_ID, AttributeValue.newBuilder()
        .setStringValue(TruncatableString.newBuilder().setValue("1234")).build());
    attrMap.put(AttributesReader.METHOD_FQN, AttributeValue.newBuilder()
        .setStringValue(TruncatableString.newBuilder().setValue("net.example.Bar.foo()")).build());

    return attrMap;
  }

  private Span generateSpanFromAttributesMap(final Map<String, AttributeValue> attrMap) {
    return Span.newBuilder()
        .setTraceId(
            ByteString.copyFrom("50c246ad9c9883d1558df9f19b9ae7a6", Charset.defaultCharset()))
        .setSpanId(ByteString.copyFrom("7ef83c66eabd5fbb", Charset.defaultCharset()))
        .setParentSpanId(ByteString.copyFrom("7ef83c66efe42aaa", Charset.defaultCharset()))
        .setStartTime(Timestamp.newBuilder().setSeconds(123).setNanos(456).build())
        .setEndTime(Timestamp.newBuilder().setSeconds(456).setNanos(789).build())
        .setAttributes(Span.Attributes.newBuilder().putAllAttributeMap(attrMap)).build();
  }

  @Test
  void valid() {
    final Map<String, AttributeValue> attrMap = this.generateValidAttributesMap();
    final Span valid = this.generateSpanFromAttributesMap(attrMap);
    assertTrue(this.validator.isValid(valid));
  }

  @Test
  void invalidLandscapeTokenValue() {
    Map<String, AttributeValue> attrMap = this.generateValidAttributesMap();

    // no token value
    attrMap.remove(AttributesReader.LANDSCAPE_TOKEN);
    Span invalid = this.generateSpanFromAttributesMap(attrMap);
    assertFalse(this.validator.isValid(invalid));

    for (final String invalidTokenValue : new String[] {"", "\n", "\t", " "}) {
      attrMap = this.generateValidAttributesMap();
      attrMap.put(AttributesReader.LANDSCAPE_TOKEN, AttributeValue.newBuilder()
          .setStringValue(TruncatableString.newBuilder().setValue(invalidTokenValue)).build());
      invalid = this.generateSpanFromAttributesMap(attrMap);
      assertFalse(this.validator.isValid(invalid));
    }
  }

  @Test
  void invalidLandscapeTokenSecret() {
    Map<String, AttributeValue> attrMap = this.generateValidAttributesMap();

    // no token value
    attrMap.remove(AttributesReader.TOKEN_SECRET);
    Span invalid = this.generateSpanFromAttributesMap(attrMap);
    assertFalse(this.validator.isValid(invalid));

    for (final String invalidTokenSecret : new String[] {"", "\n", "\t", " "}) {
      attrMap = this.generateValidAttributesMap();
      attrMap.put(AttributesReader.TOKEN_SECRET, AttributeValue.newBuilder()
          .setStringValue(TruncatableString.newBuilder().setValue(invalidTokenSecret)).build());
      invalid = this.generateSpanFromAttributesMap(attrMap);
      assertFalse(this.validator.isValid(invalid));
    }
  }

  @Test
  void testHost() {
    Map<String, AttributeValue> attrMap = this.generateValidAttributesMap();

    // no host name -> default name
    attrMap.remove(AttributesReader.HOST_NAME);
    Span invalid = this.generateSpanFromAttributesMap(attrMap);
    assertTrue(this.validator.isValid(invalid));

    // no host ip -> default ip
    attrMap = this.generateValidAttributesMap();
    attrMap.remove(AttributesReader.HOST_IP);
    invalid = this.generateSpanFromAttributesMap(attrMap);
    assertTrue(this.validator.isValid(invalid));

    // dot host ip
    attrMap = this.generateValidAttributesMap();
    attrMap.put(AttributesReader.HOST_IP, AttributeValue.newBuilder()
        .setStringValue(TruncatableString.newBuilder().setValue(" ")).build());
    invalid = this.generateSpanFromAttributesMap(attrMap);
    assertFalse(this.validator.isValid(invalid));

    for (final String invalidHostName : new String[] {"", "\n", "\t", " "}) {
      for (final String invalidHostIp : new String[] {"", "\t", "\n", " "}) {
        attrMap = this.generateValidAttributesMap();
        attrMap.put(AttributesReader.HOST_NAME, AttributeValue.newBuilder()
            .setStringValue(TruncatableString.newBuilder().setValue(invalidHostName)).build());
        attrMap.put(AttributesReader.HOST_IP, AttributeValue.newBuilder()
            .setStringValue(TruncatableString.newBuilder().setValue(invalidHostIp)).build());
        invalid = this.generateSpanFromAttributesMap(attrMap);
        assertFalse(this.validator.isValid(invalid));
      }
    }
  }

  @Test
  void testApp() {
    Map<String, AttributeValue> attrMap = this.generateValidAttributesMap();

    // no app name -> default id
    attrMap.remove(AttributesReader.APPLICATION_NAME);
    Span invalid = this.generateSpanFromAttributesMap(attrMap);
    assertTrue(this.validator.isValid(invalid));

    // no app language -> default language
    attrMap = this.generateValidAttributesMap();
    attrMap.remove(AttributesReader.APPLICATION_LANGUAGE);
    invalid = this.generateSpanFromAttributesMap(attrMap);
    assertTrue(this.validator.isValid(invalid));

    // dot app name
    attrMap = this.generateValidAttributesMap();
    attrMap.put(AttributesReader.APPLICATION_NAME, AttributeValue.newBuilder()
        .setStringValue(TruncatableString.newBuilder().setValue(" ")).build());
    invalid = this.generateSpanFromAttributesMap(attrMap);
    assertFalse(this.validator.isValid(invalid));

    // dot app language
    attrMap = this.generateValidAttributesMap();
    attrMap.put(AttributesReader.APPLICATION_LANGUAGE, AttributeValue.newBuilder()
        .setStringValue(TruncatableString.newBuilder().setValue(" ")).build());
    invalid = this.generateSpanFromAttributesMap(attrMap);
    assertFalse(this.validator.isValid(invalid));

    for (final String invalidId : new String[] {"", "\n", "\t", " "}) {
      for (final String invalidLanguage : new String[] {"", "\t", "\n", " "}) {
        attrMap = this.generateValidAttributesMap();
        attrMap.put(AttributesReader.APPLICATION_NAME, AttributeValue.newBuilder()
            .setStringValue(TruncatableString.newBuilder().setValue(invalidId)).build());
        attrMap.put(AttributesReader.APPLICATION_LANGUAGE, AttributeValue.newBuilder()
            .setStringValue(TruncatableString.newBuilder().setValue(invalidLanguage)).build());
        invalid = this.generateSpanFromAttributesMap(attrMap);
        assertFalse(this.validator.isValid(invalid));
      }
    }
  }

  @Test
  void testOperation() {
    Map<String, AttributeValue> attrMap = this.generateValidAttributesMap();

    // no method fqn -> default fqn
    attrMap.remove(AttributesReader.METHOD_FQN);
    Span invalid = this.generateSpanFromAttributesMap(attrMap);
    assertTrue(this.validator.isValid(invalid));

    for (final String invalidId : new String[] {"", "\n", "\t", " ", "noMethod",
        "classNoPackage.method", "...", "a..", "a.b.", "a.b. ", "a..c", ".b.c", "..c", ".b."}) {
      attrMap = this.generateValidAttributesMap();
      attrMap.put(AttributesReader.METHOD_FQN, AttributeValue.newBuilder()
          .setStringValue(TruncatableString.newBuilder().setValue(invalidId)).build());
      invalid = this.generateSpanFromAttributesMap(attrMap);
      assertFalse(this.validator.isValid(invalid));
    }
  }

  @Test
  void testTimestamps() {
    final Map<String, AttributeValue> attrMap = this.generateValidAttributesMap();

    Span invalid = Span.newBuilder()
        .setTraceId(
            ByteString.copyFrom("50c246ad9c9883d1558df9f19b9ae7a6", Charset.defaultCharset()))
        .setSpanId(ByteString.copyFrom("7ef83c66eabd5fbb", Charset.defaultCharset()))
        .setParentSpanId(ByteString.copyFrom("7ef83c66efe42aaa", Charset.defaultCharset()))
        .setStartTime(Timestamp.newBuilder().setSeconds(0).setNanos(0).build())
        .setEndTime(Timestamp.newBuilder().setSeconds(456).setNanos(789).build())
        .setAttributes(Span.Attributes.newBuilder().putAllAttributeMap(attrMap)).build();

    assertFalse(this.validator.isValid(invalid));

    invalid = Span.newBuilder()
        .setTraceId(
            ByteString.copyFrom("50c246ad9c9883d1558df9f19b9ae7a6", Charset.defaultCharset()))
        .setSpanId(ByteString.copyFrom("7ef83c66eabd5fbb", Charset.defaultCharset()))
        .setParentSpanId(ByteString.copyFrom("7ef83c66efe42aaa", Charset.defaultCharset()))
        .setStartTime(Timestamp.newBuilder().setSeconds(456).setNanos(789).build())
        .setEndTime(Timestamp.newBuilder().setSeconds(0).setNanos(0).build())
        .setAttributes(Span.Attributes.newBuilder().putAllAttributeMap(attrMap)).build();

    assertFalse(this.validator.isValid(invalid));

    invalid = Span.newBuilder()
        .setTraceId(
            ByteString.copyFrom("50c246ad9c9883d1558df9f19b9ae7a6", Charset.defaultCharset()))
        .setSpanId(ByteString.copyFrom("7ef83c66eabd5fbb", Charset.defaultCharset()))
        .setParentSpanId(ByteString.copyFrom("7ef83c66efe42aaa", Charset.defaultCharset()))
        .setStartTime(Timestamp.newBuilder().setSeconds(0).setNanos(0).build())
        .setEndTime(Timestamp.newBuilder().setSeconds(0).setNanos(0).build())
        .setAttributes(Span.Attributes.newBuilder().putAllAttributeMap(attrMap)).build();

    assertFalse(this.validator.isValid(invalid));

  }

  @Test
  void testValidTokenServiceDelegation() {
    final Map<String, AttributeValue> attrMap = this.generateValidAttributesMap();

    final Span valid = this.generateSpanFromAttributesMap(attrMap);

    this.validator.validateTokens = true;

    assertTrue(this.validator.isValid(valid));
  }

  @Test
  void testInvalidTokenServiceDelegation() {
    final Map<String, AttributeValue> attrMap = this.generateValidAttributesMap();

    final Span valid = this.generateSpanFromAttributesMap(attrMap);

    final TokenService mockTokenService = Mockito.mock(TokenService.class);
    Mockito.when(mockTokenService.validLandscapeTokenValueAndSecret(Matchers.anyString(),
        Matchers.anyString())).thenReturn(false);
    this.validator = new StrictValidator(mockTokenService);
    this.validator.validateTokens = true;

    assertFalse(this.validator.isValid(valid));
  }
}
