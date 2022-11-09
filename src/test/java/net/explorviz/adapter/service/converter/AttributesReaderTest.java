package net.explorviz.adapter.service.converter;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.protobuf.ByteString;
import io.opentelemetry.proto.common.v1.AnyValue;
import io.opentelemetry.proto.common.v1.KeyValue;
import io.opentelemetry.proto.trace.v1.Span;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

public class AttributesReaderTest {

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


  @Test
  void testValidSpanReadOut() {
    List<KeyValue> attr = generateValidAttributesMap();
    Span span = generateSpanFromAttributesMap(attr);
    final AttributesReader reader = new AttributesReader(span);

    assertEquals(reader.getLandscapeToken(), TOKEN);
    assertEquals(reader.getSecret(), SECRET);
    assertEquals(reader.getHostName(), HOSTNAME);
    assertEquals(reader.getHostIpAddress(), HOST_IP);
    assertEquals(reader.getApplicationName(), APP_NAME);
    assertEquals(reader.getApplicationInstanceId(), APP_INSTANCE_ID);
    assertEquals(reader.getApplicationLanguage(), APP_LANG);
    assertEquals(reader.getMethodFqn(), FQN);
  }

  @Test
  void testDefaultTokenReadOut() {
    List<KeyValue> attr = generateValidAttributesMap();
    attr = removeElementAndReturnAttributesMap(KEY_LANDSCAPE_TOKEN, attr);
    Span span = generateSpanFromAttributesMap(attr);
    final AttributesReader reader = new AttributesReader(span);

    assertEquals(reader.getLandscapeToken(), DefaultAttributeValues.DEFAULT_LANDSCAPE_TOKEN);
    assertEquals(reader.getSecret(), SECRET);
    assertEquals(reader.getHostName(), HOSTNAME);
    assertEquals(reader.getHostIpAddress(), HOST_IP);
    assertEquals(reader.getApplicationName(), APP_NAME);
    assertEquals(reader.getApplicationInstanceId(), APP_INSTANCE_ID);
    assertEquals(reader.getApplicationLanguage(), APP_LANG);
    assertEquals(reader.getMethodFqn(), FQN);
  }

  @Test
  void testDefaultSecretReadOut() {
    List<KeyValue> attr = generateValidAttributesMap();
    attr = removeElementAndReturnAttributesMap(KEY_LANDSCAPE_SECRET, attr);
    Span span = generateSpanFromAttributesMap(attr);
    final AttributesReader reader = new AttributesReader(span);

    assertEquals(reader.getLandscapeToken(), TOKEN);
    assertEquals(reader.getSecret(), DefaultAttributeValues.DEFAULT_LANDSCAPE_SECRET);
    assertEquals(reader.getHostName(), HOSTNAME);
    assertEquals(reader.getHostIpAddress(), HOST_IP);
    assertEquals(reader.getApplicationName(), APP_NAME);
    assertEquals(reader.getApplicationInstanceId(), APP_INSTANCE_ID);
    assertEquals(reader.getApplicationLanguage(), APP_LANG);
    assertEquals(reader.getMethodFqn(), FQN);
  }

  @Test
  void testDefaultHostnameReadOut() {
    List<KeyValue> attr = generateValidAttributesMap();
    attr = removeElementAndReturnAttributesMap(KEY_HOST_NAME, attr);
    Span span = generateSpanFromAttributesMap(attr);
    final AttributesReader reader = new AttributesReader(span);

    assertEquals(reader.getLandscapeToken(), TOKEN);
    assertEquals(reader.getSecret(), SECRET);
    assertEquals(reader.getHostName(), DefaultAttributeValues.DEFAULT_HOST_NAME);
    assertEquals(reader.getHostIpAddress(), HOST_IP);
    assertEquals(reader.getApplicationName(), APP_NAME);
    assertEquals(reader.getApplicationInstanceId(), APP_INSTANCE_ID);
    assertEquals(reader.getApplicationLanguage(), APP_LANG);
    assertEquals(reader.getMethodFqn(), FQN);
  }

  @Test
  void testDefaultHostIpReadOut() {
    List<KeyValue> attr = generateValidAttributesMap();
    attr = removeElementAndReturnAttributesMap(KEY_HOST_IP, attr);
    Span span = generateSpanFromAttributesMap(attr);
    final AttributesReader reader = new AttributesReader(span);

    assertEquals(reader.getLandscapeToken(), TOKEN);
    assertEquals(reader.getSecret(), SECRET);
    assertEquals(reader.getHostName(), HOSTNAME);
    assertEquals(reader.getHostIpAddress(), DefaultAttributeValues.DEFAULT_HOST_IP);
    assertEquals(reader.getApplicationName(), APP_NAME);
    assertEquals(reader.getApplicationInstanceId(), APP_INSTANCE_ID);
    assertEquals(reader.getApplicationLanguage(), APP_LANG);
    assertEquals(reader.getMethodFqn(), FQN);
  }

  @Test
  void testDefaultAppNameReadOut() {
    List<KeyValue> attr = generateValidAttributesMap();
    attr = removeElementAndReturnAttributesMap(KEY_APPLICATION_NAME, attr);
    Span span = generateSpanFromAttributesMap(attr);
    final AttributesReader reader = new AttributesReader(span);

    assertEquals(reader.getLandscapeToken(), TOKEN);
    assertEquals(reader.getSecret(), SECRET);
    assertEquals(reader.getHostName(), HOSTNAME);
    assertEquals(reader.getHostIpAddress(), HOST_IP);
    assertEquals(reader.getApplicationName(), DefaultAttributeValues.DEFAULT_APP_NAME);
    assertEquals(reader.getApplicationInstanceId(), APP_INSTANCE_ID);
    assertEquals(reader.getApplicationLanguage(), APP_LANG);
    assertEquals(reader.getMethodFqn(), FQN);
  }

  @Test
  void testDefaultAppInstanceIdReadOut() {
    List<KeyValue> attr = generateValidAttributesMap();
    attr = removeElementAndReturnAttributesMap(KEY_APPLICATION_INSTANCE_ID, attr);
    Span span = generateSpanFromAttributesMap(attr);
    final AttributesReader reader = new AttributesReader(span);

    assertEquals(reader.getLandscapeToken(), TOKEN);
    assertEquals(reader.getSecret(), SECRET);
    assertEquals(reader.getHostName(), HOSTNAME);
    assertEquals(reader.getHostIpAddress(), HOST_IP);
    assertEquals(reader.getApplicationName(), APP_NAME);
    assertEquals(reader.getApplicationInstanceId(), DefaultAttributeValues.DEFAULT_APP_INSTANCE_ID);
    assertEquals(reader.getApplicationLanguage(), APP_LANG);
    assertEquals(reader.getMethodFqn(), FQN);
  }

  @Test
  void testDefaultAppLangReadOut() {
    List<KeyValue> attr = generateValidAttributesMap();
    attr = removeElementAndReturnAttributesMap(KEY_APPLICATION_LANGUAGE, attr);
    Span span = generateSpanFromAttributesMap(attr);
    final AttributesReader reader = new AttributesReader(span);

    assertEquals(reader.getLandscapeToken(), TOKEN);
    assertEquals(reader.getSecret(), SECRET);
    assertEquals(reader.getHostName(), HOSTNAME);
    assertEquals(reader.getHostIpAddress(), HOST_IP);
    assertEquals(reader.getApplicationName(), APP_NAME);
    assertEquals(reader.getApplicationInstanceId(), APP_INSTANCE_ID);
    assertEquals(reader.getApplicationLanguage(), DefaultAttributeValues.DEFAULT_APP_LANG);
    assertEquals(reader.getMethodFqn(), FQN);
  }

  @Test
  void testDefaultFqnReadOut() {
    List<KeyValue> attr = generateValidAttributesMap();
    attr = removeElementAndReturnAttributesMap(KEY_METHOD_FQN, attr);
    Span span = generateSpanFromAttributesMap(attr);
    final AttributesReader reader = new AttributesReader(span);

    assertEquals(reader.getLandscapeToken(), TOKEN);
    assertEquals(reader.getSecret(), SECRET);
    assertEquals(reader.getHostName(), HOSTNAME);
    assertEquals(reader.getHostIpAddress(), HOST_IP);
    assertEquals(reader.getApplicationName(), APP_NAME);
    assertEquals(reader.getApplicationInstanceId(), APP_INSTANCE_ID);
    assertEquals(reader.getApplicationLanguage(), APP_LANG);
    assertEquals(reader.getMethodFqn(), DefaultAttributeValues.DEFAULT_FQN);
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

}
