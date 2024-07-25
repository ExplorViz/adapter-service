package net.explorviz.adapter.service.validation;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.protobuf.ByteString;
import io.opentelemetry.proto.common.v1.AnyValue;
import io.opentelemetry.proto.common.v1.KeyValue;
import io.opentelemetry.proto.trace.v1.Span;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import net.explorviz.adapter.service.TokenService;
import net.explorviz.adapter.service.converter.AttributesReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;

class DefaultSpanValidatorTest {

  private static final String KEY_LANDSCAPE_TOKEN = AttributesReader.LANDSCAPE_TOKEN;
  private static final String KEY_LANDSCAPE_SECRET = AttributesReader.TOKEN_SECRET;
  private static final String KEY_HOST_NAME = AttributesReader.HOST_NAME;
  private static final String KEY_HOST_IP = AttributesReader.HOST_IP;
  private static final String KEY_APPLICATION_NAME = AttributesReader.APPLICATION_NAME;
  private static final String KEY_APPLICATION_INSTANCE_ID =
      AttributesReader.APPLICATION_INSTANCE_ID;
  private static final String KEY_APPLICATION_LANGUAGE = AttributesReader.APPLICATION_LANGUAGE;
  private static final String KEY_METHOD_FQN = AttributesReader.METHOD_FQN;

  private static final String TOKEN = "tok";
  private static final String SECRET = "secret";
  private static final String HOSTNAME = "Host";
  private static final String HOST_IP = "1.2.3.4";
  private static final String APP_NAME = "Test App";
  private static final String APP_INSTANCE_ID = "1234L";
  private static final String APP_LANG = "java";
  private static final String FQN = "foo.bar.test()";

  private DefaultSpanValidator validator;
  private AttributesReader validSpan;

  @BeforeEach
  void setUp() {
    final TokenService mockTokenService = Mockito.mock(TokenService.class);
    Mockito.when(mockTokenService.validLandscapeTokenValueAndSecret(Matchers.anyString(),
        Matchers.anyString())).thenReturn(true);
    this.validator = new DefaultSpanValidator(mockTokenService);

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

  private List<KeyValue> generateValidAttributesMap() {
    List<KeyValue> attributes = new ArrayList<>();

    attributes.add(KeyValue.newBuilder().setKey(KEY_LANDSCAPE_TOKEN)
        .setValue(AnyValue.newBuilder().setStringValue(TOKEN).build()).build());

    attributes.add(KeyValue.newBuilder().setKey(KEY_LANDSCAPE_SECRET)
        .setValue(AnyValue.newBuilder().setStringValue(SECRET).build()).build());

    attributes.add(KeyValue.newBuilder().setKey(KEY_HOST_NAME)
        .setValue(AnyValue.newBuilder().setStringValue(HOSTNAME).build()).build());

    attributes.add(KeyValue.newBuilder().setKey(KEY_HOST_IP)
        .setValue(AnyValue.newBuilder().setStringValue(HOST_IP).build()).build());

    attributes.add(KeyValue.newBuilder().setKey(KEY_APPLICATION_NAME)
        .setValue(AnyValue.newBuilder().setStringValue(APP_NAME).build()).build());

    attributes.add(KeyValue.newBuilder().setKey(KEY_APPLICATION_INSTANCE_ID)
        .setValue(AnyValue.newBuilder().setStringValue(APP_INSTANCE_ID).build()).build());

    attributes.add(KeyValue.newBuilder().setKey(KEY_APPLICATION_LANGUAGE)
        .setValue(AnyValue.newBuilder().setStringValue(APP_LANG).build()).build());

    attributes.add(KeyValue.newBuilder().setKey(KEY_METHOD_FQN)
        .setValue(AnyValue.newBuilder().setStringValue(FQN).build()).build());

    return attributes;
  }

  private Span generateSpanFromAttributesMap(List<KeyValue> attributes) {
    return Span.newBuilder()
        .setTraceId(
            ByteString.copyFrom("50c246ad9c9883d1558df9f19b9ae7a6", Charset.defaultCharset()))
        .setSpanId(ByteString.copyFrom("7ef83c66eabd5fbb", Charset.defaultCharset()))
        .setParentSpanId(ByteString.copyFrom("7ef83c66efe42aaa", Charset.defaultCharset()))
        .setStartTimeUnixNano(1667986986000L)
        .setEndTimeUnixNano(1667987046000L)
        .addAllAttributes(attributes).build();
  }

  private List<KeyValue> removeElementAndReturnAttributesMap(String keyToBeRemoved,
      List<KeyValue> attributes) {

    List<KeyValue> resultList = new ArrayList<>();

    for (KeyValue keyVal : attributes) {
      if (keyVal.getKey().equals(keyToBeRemoved)) {
        // do nothing
      } else {
        resultList.add(keyVal);
      }
    }

    return resultList;
  }

  private List<KeyValue> replaceElementAndReturnAttributesMap(String key, String newVal,
      List<KeyValue> attributes) {

    List<KeyValue> resultList = new ArrayList<>();

    for (KeyValue keyVal : attributes) {
      if (keyVal.getKey().equals(key)) {
        resultList.add(
            KeyValue.newBuilder().setKey(key).setValue(AnyValue.newBuilder().setStringValue(newVal))
                .build());
      } else {
        resultList.add(keyVal);
      }
    }

    return resultList;
  }

  @Test
  void valid() {
    final List<KeyValue> attrMap = this.generateValidAttributesMap();
    final Span valid = this.generateSpanFromAttributesMap(attrMap);
    assertTrue(this.validator.isValid(valid));
  }

  @Test
  void invalidLandscapeTokenValue() {
    for (final String invalidTokenValue : new String[] {"", "\n", "\t", " "}) {
      List<KeyValue> attrMap = this.generateValidAttributesMap();
      attrMap = this.replaceElementAndReturnAttributesMap(KEY_LANDSCAPE_TOKEN, invalidTokenValue,
          attrMap);
      Span invalid = this.generateSpanFromAttributesMap(attrMap);
      assertFalse(this.validator.isValid(invalid));
    }
  }

  @Test
  void invalidLandscapeTokenSecret() {
    for (final String invalidTokenSecret : new String[] {"", "\n", "\t", " "}) {
      List<KeyValue> attrMap = this.generateValidAttributesMap();
      attrMap = this.replaceElementAndReturnAttributesMap(KEY_LANDSCAPE_SECRET, invalidTokenSecret,
          attrMap);
      Span invalid = this.generateSpanFromAttributesMap(attrMap);
      assertFalse(this.validator.isValid(invalid));
    }
  }

  @Test
  void testHost() {
    List<KeyValue> attrMap = this.generateValidAttributesMap();

    // no host name -> default name
    attrMap = this.removeElementAndReturnAttributesMap(KEY_HOST_NAME, attrMap);
    Span invalid = this.generateSpanFromAttributesMap(attrMap);
    assertTrue(this.validator.isValid(invalid));

    // no host ip -> default ip
    attrMap = this.generateValidAttributesMap();
    attrMap = this.removeElementAndReturnAttributesMap(KEY_HOST_IP, attrMap);
    invalid = this.generateSpanFromAttributesMap(attrMap);
    assertTrue(this.validator.isValid(invalid));

    // no dot host ip
    attrMap = this.generateValidAttributesMap();
    attrMap = this.replaceElementAndReturnAttributesMap(KEY_HOST_IP, " ",
        attrMap);
    invalid = this.generateSpanFromAttributesMap(attrMap);
    assertFalse(this.validator.isValid(invalid));

    // Test different combinations
    for (final String invalidHostName : new String[] {"", "\n", "\t", " "}) {
      for (final String invalidHostIp : new String[] {"", "\t", "\n", " "}) {
        attrMap = this.generateValidAttributesMap();
        attrMap = this.replaceElementAndReturnAttributesMap(KEY_HOST_NAME, invalidHostName,
            attrMap);
        attrMap = this.replaceElementAndReturnAttributesMap(KEY_HOST_IP, invalidHostIp,
            attrMap);
        invalid = this.generateSpanFromAttributesMap(attrMap);
        assertFalse(this.validator.isValid(invalid));
      }
    }
  }

  @Test
  void testApp() {
    List<KeyValue> attrMap = this.generateValidAttributesMap();

    // no app name -> default id
    attrMap = this.removeElementAndReturnAttributesMap(KEY_APPLICATION_NAME, attrMap);
    Span invalid = this.generateSpanFromAttributesMap(attrMap);
    assertTrue(this.validator.isValid(invalid));

    // no app language -> default language
    attrMap = this.generateValidAttributesMap();
    attrMap = this.removeElementAndReturnAttributesMap(KEY_APPLICATION_LANGUAGE, attrMap);
    invalid = this.generateSpanFromAttributesMap(attrMap);
    assertTrue(this.validator.isValid(invalid));

    // dot app name
    attrMap = this.generateValidAttributesMap();
    attrMap = this.replaceElementAndReturnAttributesMap(KEY_APPLICATION_NAME, " ", attrMap);
    invalid = this.generateSpanFromAttributesMap(attrMap);
    assertFalse(this.validator.isValid(invalid));

    // dot app language
    attrMap = this.generateValidAttributesMap();
    attrMap = this.replaceElementAndReturnAttributesMap(KEY_APPLICATION_LANGUAGE, " ", attrMap);
    invalid = this.generateSpanFromAttributesMap(attrMap);
    assertFalse(this.validator.isValid(invalid));

    for (final String invalidId : new String[] {"", "\n", "\t", " "}) {
      for (final String invalidLanguage : new String[] {"", "\t", "\n", " "}) {
        attrMap = this.generateValidAttributesMap();
        attrMap = this.replaceElementAndReturnAttributesMap(KEY_APPLICATION_NAME, invalidId,
            attrMap);
        attrMap = this.replaceElementAndReturnAttributesMap(KEY_APPLICATION_LANGUAGE,
            invalidLanguage, attrMap);
        invalid = this.generateSpanFromAttributesMap(attrMap);
        assertFalse(this.validator.isValid(invalid));
      }
    }
  }

  @Test
  void testOperation() {
    List<KeyValue> attrMap = this.generateValidAttributesMap();
    
    Span invalid = this.generateSpanFromAttributesMap(attrMap);

    for (final String invalidMethodFqn : new String[] {"", "\n", "\t", " ", "noMethod",
        "classNoPackage.method", "...", "a..", "a.b.", "a.b. ", "a..c", ".b.c", "..c", ".b."}) {
      attrMap = this.generateValidAttributesMap();
      attrMap = this.replaceElementAndReturnAttributesMap(KEY_METHOD_FQN,
          invalidMethodFqn, attrMap);
      invalid = this.generateSpanFromAttributesMap(attrMap);
      assertFalse(this.validator.isValid(invalid));
    }
  }

  @Test
  void testTimestamps() {
    List<KeyValue> attrMap = this.generateValidAttributesMap();

    Span invalid = Span.newBuilder()
        .setTraceId(
            ByteString.copyFrom("50c246ad9c9883d1558df9f19b9ae7a6", Charset.defaultCharset()))
        .setSpanId(ByteString.copyFrom("7ef83c66eabd5fbb", Charset.defaultCharset()))
        .setParentSpanId(ByteString.copyFrom("7ef83c66efe42aaa", Charset.defaultCharset()))
        .setStartTimeUnixNano(0L)
        .setEndTimeUnixNano(456L)
        .addAllAttributes(attrMap).build();

    assertFalse(this.validator.isValid(invalid));

    invalid = Span.newBuilder()
        .setTraceId(
            ByteString.copyFrom("50c246ad9c9883d1558df9f19b9ae7a6", Charset.defaultCharset()))
        .setSpanId(ByteString.copyFrom("7ef83c66eabd5fbb", Charset.defaultCharset()))
        .setParentSpanId(ByteString.copyFrom("7ef83c66efe42aaa", Charset.defaultCharset()))
        .setStartTimeUnixNano(456L)
        .setEndTimeUnixNano(0L)
        .addAllAttributes(attrMap).build();

    assertFalse(this.validator.isValid(invalid));

    invalid = Span.newBuilder()
        .setTraceId(
            ByteString.copyFrom("50c246ad9c9883d1558df9f19b9ae7a6", Charset.defaultCharset()))
        .setSpanId(ByteString.copyFrom("7ef83c66eabd5fbb", Charset.defaultCharset()))
        .setParentSpanId(ByteString.copyFrom("7ef83c66efe42aaa", Charset.defaultCharset()))
        .setStartTimeUnixNano(0L)
        .setEndTimeUnixNano(0L)
        .addAllAttributes(attrMap).build();

    assertFalse(this.validator.isValid(invalid));

  }

  @Test
  void testValidTokenServiceDelegation() {
    List<KeyValue> attrMap = this.generateValidAttributesMap();

    final Span valid = this.generateSpanFromAttributesMap(attrMap);

    this.validator.validateTokens = true;

    assertTrue(this.validator.isValid(valid));
  }

  @Test
  void testInvalidTokenServiceDelegation() {
    List<KeyValue> attrMap = this.generateValidAttributesMap();

    final Span valid = this.generateSpanFromAttributesMap(attrMap);

    final TokenService mockTokenService = Mockito.mock(TokenService.class);
    Mockito.when(mockTokenService.validLandscapeTokenValueAndSecret(Matchers.anyString(),
        Matchers.anyString())).thenReturn(false);
    this.validator = new DefaultSpanValidator(mockTokenService);
    this.validator.validateTokens = true;

    assertFalse(this.validator.isValid(valid));
  }

  @Test
  void testValidK8sStuffWithNonEmpty() {
    List<KeyValue> attrMap = this.generateValidAttributesMap();
    attrMap.add(newKeyValueString(AttributesReader.K8S_POD_NAME, "pod1"));
    attrMap.add(newKeyValueString(AttributesReader.K8S_DEPLOYMENT_NAME, "deployment1"));
    attrMap.add(newKeyValueString(AttributesReader.K8S_NODE_NAME, "node1"));
    attrMap.add(newKeyValueString(AttributesReader.K8S_NAMESPACE_NAME, "namespace1"));
    final Span s = this.generateSpanFromAttributesMap(attrMap);
    assertTrue(this.validator.isValid(s));
  }

  @Test
  void testValidK8sStuffWithEmptyActivelySet() {
    List<KeyValue> attrMap = this.generateValidAttributesMap();
    attrMap.add(newKeyValueString(AttributesReader.K8S_POD_NAME, ""));
    attrMap.add(newKeyValueString(AttributesReader.K8S_DEPLOYMENT_NAME, ""));
    attrMap.add(newKeyValueString(AttributesReader.K8S_NODE_NAME, ""));
    attrMap.add(newKeyValueString(AttributesReader.K8S_NAMESPACE_NAME, ""));
    final Span s = this.generateSpanFromAttributesMap(attrMap);
    assertTrue(this.validator.isValid(s));
  }

  @Test
  void testValidK8sStuffWithEmptyUnset() {
    List<KeyValue> attrMap = this.generateValidAttributesMap();
    final Span s = this.generateSpanFromAttributesMap(attrMap);
    assertTrue(this.validator.isValid(s));
  }

  @Test
  void testInvalidK8sStuff() {
    // iter over all possible combinations of invalid set/not set combinations
    for (var i = 0b0001; i <= 0b1110; i++) {
      List<KeyValue> attrMap = this.generateValidAttributesMap();

      if ((i & 0b0001) != 0) {
        attrMap.add(newKeyValueString(AttributesReader.K8S_POD_NAME, "pod1"));
      }
      if ((i & 0b0010) != 0) {
        attrMap.add(newKeyValueString(AttributesReader.K8S_DEPLOYMENT_NAME, "deployment1"));
      }
      if ((i & 0b0100) != 0) {
        attrMap.add(newKeyValueString(AttributesReader.K8S_NODE_NAME, "node1"));
      }
      if ((i & 0b1000) != 0) {
        attrMap.add(newKeyValueString(AttributesReader.K8S_NAMESPACE_NAME, "namespace1"));
      }

      final Span s = this.generateSpanFromAttributesMap(attrMap);
      assertFalse(this.validator.isValid(s));
    }
  }

  public static KeyValue newKeyValueString(String key, String value) {
    return KeyValue.newBuilder().setKey(key)
        .setValue(AnyValue.newBuilder().setStringValue(value)).build();
  }
}
