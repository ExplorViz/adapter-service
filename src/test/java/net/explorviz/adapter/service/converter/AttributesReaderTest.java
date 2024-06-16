package net.explorviz.adapter.service.converter;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.protobuf.ByteString;
import io.opentelemetry.proto.common.v1.AnyValue;
import io.opentelemetry.proto.common.v1.KeyValue;
import io.opentelemetry.proto.trace.v1.Span;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import net.explorviz.adapter.service.validation.StrictValidator;
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
  private static final String K8S_POD_NAME = "my name is pod. james pod.";
  private static final String K8S_NAMESPACE = "name ";
  private static final String K8S_DEPLOYMENT = "iraq. i committed war crimes there, ordered by the US government.";
  private static final String K8S_NODE = "node.js";




  @Test
  void testValidSpanReadOut() {
    List<KeyValue> attr = generateValidAttributesMap();
    Span span = generateSpanFromAttributesMap(attr);
    final AttributesReader reader = new AttributesReader(span);

    assertExcept(reader, null);
    
  }

  @Test
  void testDefaultTokenReadOut() {
    List<KeyValue> attr = generateValidAttributesMap();
    attr = removeElementAndReturnAttributesMap(KEY_LANDSCAPE_TOKEN, attr);
    Span span = generateSpanFromAttributesMap(attr);
    final AttributesReader reader = new AttributesReader(span);
    
    assertExcept(reader, AttributesReader.LANDSCAPE_TOKEN);
    assertEquals(reader.getLandscapeToken(), DefaultAttributeValues.DEFAULT_LANDSCAPE_TOKEN);
  }

  @Test
  void testDefaultSecretReadOut() {
    List<KeyValue> attr = generateValidAttributesMap();
    attr = removeElementAndReturnAttributesMap(KEY_LANDSCAPE_SECRET, attr);
    Span span = generateSpanFromAttributesMap(attr);
    final AttributesReader reader = new AttributesReader(span);
    
    assertExcept(reader, AttributesReader.TOKEN_SECRET);
    assertEquals(reader.getSecret(), DefaultAttributeValues.DEFAULT_LANDSCAPE_SECRET);
  }

  @Test
  void testDefaultHostnameReadOut() {
    List<KeyValue> attr = generateValidAttributesMap();
    attr = removeElementAndReturnAttributesMap(KEY_HOST_NAME, attr);
    Span span = generateSpanFromAttributesMap(attr);
    final AttributesReader reader = new AttributesReader(span);

    assertExcept(reader, AttributesReader.HOST_NAME);
    assertEquals(reader.getHostName(), DefaultAttributeValues.DEFAULT_HOST_NAME);
  }

  @Test
  void testDefaultHostIpReadOut() {
    List<KeyValue> attr = generateValidAttributesMap();
    attr = removeElementAndReturnAttributesMap(KEY_HOST_IP, attr);
    Span span = generateSpanFromAttributesMap(attr);
    final AttributesReader reader = new AttributesReader(span);

    assertExcept(reader, AttributesReader.HOST_IP);
    assertEquals(reader.getHostIpAddress(), DefaultAttributeValues.DEFAULT_HOST_IP);
  }

  @Test
  void testDefaultAppNameReadOut() {
    List<KeyValue> attr = generateValidAttributesMap();
    attr = removeElementAndReturnAttributesMap(KEY_APPLICATION_NAME, attr);
    Span span = generateSpanFromAttributesMap(attr);
    final AttributesReader reader = new AttributesReader(span);

    assertExcept(reader, AttributesReader.APPLICATION_NAME);
    assertEquals(reader.getApplicationName(), DefaultAttributeValues.DEFAULT_APP_NAME);
  }

  @Test
  void testDefaultAppInstanceIdReadOut() {
    List<KeyValue> attr = generateValidAttributesMap();
    attr = removeElementAndReturnAttributesMap(KEY_APPLICATION_INSTANCE_ID, attr);
    Span span = generateSpanFromAttributesMap(attr);
    final AttributesReader reader = new AttributesReader(span);

    assertExcept(reader, AttributesReader.APPLICATION_INSTANCE_ID);
    assertEquals(reader.getApplicationInstanceId(), DefaultAttributeValues.DEFAULT_APP_INSTANCE_ID);
  }

  @Test
  void testDefaultAppLangReadOut() {
    List<KeyValue> attr = generateValidAttributesMap();
    attr = removeElementAndReturnAttributesMap(KEY_APPLICATION_LANGUAGE, attr);
    Span span = generateSpanFromAttributesMap(attr);
    final AttributesReader reader = new AttributesReader(span);

    assertExcept(reader, AttributesReader.APPLICATION_LANGUAGE);
    assertEquals(reader.getApplicationLanguage(), DefaultAttributeValues.DEFAULT_APP_LANG);
  }


  @Test
  void testDefaultFqnReadOut() {
    List<KeyValue> attr = generateValidAttributesMap();
    attr = removeElementAndReturnAttributesMap(KEY_METHOD_FQN, attr);
    Span span = generateSpanFromAttributesMap(attr);
    final AttributesReader reader = new AttributesReader(span);

    assertExcept(reader, AttributesReader.METHOD_FQN);
    assertEquals(reader.getMethodFqn(), DefaultAttributeValues.DEFAULT_FQN);
  }
  
  void assertExcept(AttributesReader reader, String except){
    if(!Objects.equals(except, AttributesReader.LANDSCAPE_TOKEN))
        assertEquals(reader.getLandscapeToken(), TOKEN);
    if(!Objects.equals(except, AttributesReader.TOKEN_SECRET))
        assertEquals(reader.getSecret(), SECRET);
    if(!Objects.equals(except, AttributesReader.HOST_NAME))
        assertEquals(reader.getHostName(), HOSTNAME);
    if(!Objects.equals(except, AttributesReader.HOST_IP))
        assertEquals(reader.getHostIpAddress(), HOST_IP);
    if(!Objects.equals(except, AttributesReader.APPLICATION_NAME))
        assertEquals(reader.getApplicationName(), APP_NAME);
    if(!Objects.equals(except, AttributesReader.APPLICATION_INSTANCE_ID))
        assertEquals(reader.getApplicationInstanceId(), APP_INSTANCE_ID);
    if(!Objects.equals(except, AttributesReader.APPLICATION_LANGUAGE))
        assertEquals(reader.getApplicationLanguage(), APP_LANG);
    if(!Objects.equals(except, AttributesReader.METHOD_FQN))
      assertEquals(reader.getMethodFqn(), FQN);
    
    // k8s stuff
    if(!Objects.equals(except, AttributesReader.K8S_POD_NAME))
        assertEquals(reader.getK8sPodName(), K8S_POD_NAME);
    if(!Objects.equals(except, AttributesReader.K8S_NAMESPACE_NAME))
        assertEquals(reader.getK8sNamespace(), K8S_NAMESPACE);
    if(!Objects.equals(except, AttributesReader.K8S_DEPLOYMENT_NAME))
        assertEquals(reader.getK8sDeploymentName(), K8S_DEPLOYMENT);
    if(!Objects.equals(except, AttributesReader.K8S_NODE_NAME))
        assertEquals(reader.getK8sNodeName(), K8S_NODE);

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

    attributes.add(newKeyValueString(AttributesReader.K8S_POD_NAME, K8S_POD_NAME));
    attributes.add(newKeyValueString(AttributesReader.K8S_NAMESPACE_NAME, K8S_NAMESPACE));
    attributes.add(newKeyValueString(AttributesReader.K8S_DEPLOYMENT_NAME, K8S_DEPLOYMENT));
    attributes.add(newKeyValueString(AttributesReader.K8S_NODE_NAME, K8S_NODE));

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


  // TODO: dont do code duplication with StrictValidatorTest.newKeyValueString
  // this should be done when replacing all 
  // KeyValue.newBuilder().setKey(KEY_HOST_IP).setValue(AnyValue.newBuilder().setStringValue(HOST_IP).build()).build()
  // with this method.
  // i am not doing this here, because i am implementing a specific feature; not refactoring the whole project.
  // so pwease forgive me, uwu
  //
  // i am not quite sure if this sounds condescending.
  // if yes: it's not meant to be.
  // i cant tell since im autistic *jazz hands*
  public static KeyValue newKeyValueString(String key, String value){
    return KeyValue.newBuilder().setKey(key)
        .setValue(AnyValue.newBuilder().setStringValue(value)).build();
  }

}
