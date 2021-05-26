package net.explorviz.adapter.service.converter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import io.opencensus.proto.trace.v1.AttributeValue;
import io.opencensus.proto.trace.v1.Span;
import io.opencensus.proto.trace.v1.Span.Attributes;
import io.opencensus.proto.trace.v1.TruncatableString;
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
    final Span validSpan;

    final Attributes attr = Attributes.newBuilder()
        .putAttributeMap(KEY_LANDSCAPE_TOKEN,
            AttributeValue.newBuilder()
                .setStringValue(TruncatableString.newBuilder().setValue(TOKEN).build()).build())
        .putAttributeMap(KEY_LANDSCAPE_SECRET, AttributeValue.newBuilder()
            .setStringValue(
                TruncatableString.newBuilder().setValue(SECRET).build())
            .build())
        .putAttributeMap(KEY_HOST_NAME, AttributeValue.newBuilder()
            .setStringValue(
                TruncatableString.newBuilder().setValue(HOSTNAME).build())
            .build())
        .putAttributeMap(KEY_HOST_IP, AttributeValue.newBuilder()
            .setStringValue(
                TruncatableString.newBuilder().setValue(HOST_IP).build())
            .build())
        .putAttributeMap(KEY_APPLICATION_NAME, AttributeValue.newBuilder()
            .setStringValue(
                TruncatableString.newBuilder().setValue(APP_NAME).build())
            .build())
        .putAttributeMap(KEY_APPLICATION_INSTANCE_ID,
            AttributeValue.newBuilder()
                .setStringValue(TruncatableString.newBuilder()
                    .setValue(APP_INSTANCE_ID).build())
                .build())
        .putAttributeMap(KEY_APPLICATION_LANGUAGE, AttributeValue.newBuilder()
            .setStringValue(
                TruncatableString.newBuilder().setValue(APP_LANG).build())
            .build())
        .putAttributeMap(KEY_METHOD_FQN, AttributeValue.newBuilder()
            .setStringValue(
                TruncatableString.newBuilder().setValue(FQN).build())
            .build())
        .build();

    validSpan = Span.newBuilder().setAttributes(attr).build();

    final AttributesReader reader = new AttributesReader(validSpan);

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
    final Span validSpan;

    final Attributes attr = Attributes.newBuilder()
        .putAttributeMap(KEY_LANDSCAPE_SECRET, AttributeValue.newBuilder()
            .setStringValue(
                TruncatableString.newBuilder().setValue(SECRET).build())
            .build())
        .putAttributeMap(KEY_HOST_NAME, AttributeValue.newBuilder()
            .setStringValue(
                TruncatableString.newBuilder().setValue(HOSTNAME).build())
            .build())
        .putAttributeMap(KEY_HOST_IP, AttributeValue.newBuilder()
            .setStringValue(
                TruncatableString.newBuilder().setValue(HOST_IP).build())
            .build())
        .putAttributeMap(KEY_APPLICATION_NAME, AttributeValue.newBuilder()
            .setStringValue(
                TruncatableString.newBuilder().setValue(APP_NAME).build())
            .build())
        .putAttributeMap(KEY_APPLICATION_INSTANCE_ID,
            AttributeValue.newBuilder()
                .setStringValue(TruncatableString.newBuilder()
                    .setValue(APP_INSTANCE_ID).build())
                .build())
        .putAttributeMap(KEY_APPLICATION_LANGUAGE, AttributeValue.newBuilder()
            .setStringValue(
                TruncatableString.newBuilder().setValue(APP_LANG).build())
            .build())
        .putAttributeMap(KEY_METHOD_FQN, AttributeValue.newBuilder()
            .setStringValue(
                TruncatableString.newBuilder().setValue(FQN).build())
            .build())
        .build();

    validSpan = Span.newBuilder().setAttributes(attr).build();

    final AttributesReader reader = new AttributesReader(validSpan);

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
    final Span validSpan;

    final Attributes attr = Attributes.newBuilder()
        .putAttributeMap(KEY_LANDSCAPE_TOKEN,
            AttributeValue.newBuilder()
                .setStringValue(TruncatableString.newBuilder().setValue(TOKEN).build()).build())
        .putAttributeMap(KEY_HOST_NAME, AttributeValue.newBuilder()
            .setStringValue(
                TruncatableString.newBuilder().setValue(HOSTNAME).build())
            .build())
        .putAttributeMap(KEY_HOST_IP, AttributeValue.newBuilder()
            .setStringValue(
                TruncatableString.newBuilder().setValue(HOST_IP).build())
            .build())
        .putAttributeMap(KEY_APPLICATION_NAME, AttributeValue.newBuilder()
            .setStringValue(
                TruncatableString.newBuilder().setValue(APP_NAME).build())
            .build())
        .putAttributeMap(KEY_APPLICATION_INSTANCE_ID,
            AttributeValue.newBuilder()
                .setStringValue(TruncatableString.newBuilder()
                    .setValue(APP_INSTANCE_ID).build())
                .build())
        .putAttributeMap(KEY_APPLICATION_LANGUAGE, AttributeValue.newBuilder()
            .setStringValue(
                TruncatableString.newBuilder().setValue(APP_LANG).build())
            .build())
        .putAttributeMap(KEY_METHOD_FQN, AttributeValue.newBuilder()
            .setStringValue(
                TruncatableString.newBuilder().setValue(FQN).build())
            .build())
        .build();

    validSpan = Span.newBuilder().setAttributes(attr).build();

    final AttributesReader reader = new AttributesReader(validSpan);

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
    final Span validSpan;

    final Attributes attr = Attributes.newBuilder()
        .putAttributeMap(KEY_LANDSCAPE_TOKEN,
            AttributeValue.newBuilder()
                .setStringValue(TruncatableString.newBuilder().setValue(TOKEN).build()).build())
        .putAttributeMap(KEY_LANDSCAPE_SECRET, AttributeValue.newBuilder()
            .setStringValue(
                TruncatableString.newBuilder().setValue(SECRET).build())
            .build())
        .putAttributeMap(KEY_HOST_IP, AttributeValue.newBuilder()
            .setStringValue(
                TruncatableString.newBuilder().setValue(HOST_IP).build())
            .build())
        .putAttributeMap(KEY_APPLICATION_NAME, AttributeValue.newBuilder()
            .setStringValue(
                TruncatableString.newBuilder().setValue(APP_NAME).build())
            .build())
        .putAttributeMap(KEY_APPLICATION_INSTANCE_ID,
            AttributeValue.newBuilder()
                .setStringValue(TruncatableString.newBuilder()
                    .setValue(APP_INSTANCE_ID).build())
                .build())
        .putAttributeMap(KEY_APPLICATION_LANGUAGE, AttributeValue.newBuilder()
            .setStringValue(
                TruncatableString.newBuilder().setValue(APP_LANG).build())
            .build())
        .putAttributeMap(KEY_METHOD_FQN, AttributeValue.newBuilder()
            .setStringValue(
                TruncatableString.newBuilder().setValue(FQN).build())
            .build())
        .build();

    validSpan = Span.newBuilder().setAttributes(attr).build();

    final AttributesReader reader = new AttributesReader(validSpan);

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
    final Span validSpan;

    final Attributes attr = Attributes.newBuilder()
        .putAttributeMap(KEY_LANDSCAPE_TOKEN,
            AttributeValue.newBuilder()
                .setStringValue(TruncatableString.newBuilder().setValue(TOKEN).build()).build())
        .putAttributeMap(KEY_LANDSCAPE_SECRET, AttributeValue.newBuilder()
            .setStringValue(
                TruncatableString.newBuilder().setValue(SECRET).build())
            .build())
        .putAttributeMap(KEY_HOST_NAME, AttributeValue.newBuilder()
            .setStringValue(
                TruncatableString.newBuilder().setValue(HOSTNAME).build())
            .build())
        .putAttributeMap(KEY_APPLICATION_NAME, AttributeValue.newBuilder()
            .setStringValue(
                TruncatableString.newBuilder().setValue(APP_NAME).build())
            .build())
        .putAttributeMap(KEY_APPLICATION_INSTANCE_ID,
            AttributeValue.newBuilder()
                .setStringValue(TruncatableString.newBuilder()
                    .setValue(APP_INSTANCE_ID).build())
                .build())
        .putAttributeMap(KEY_APPLICATION_LANGUAGE, AttributeValue.newBuilder()
            .setStringValue(
                TruncatableString.newBuilder().setValue(APP_LANG).build())
            .build())
        .putAttributeMap(KEY_METHOD_FQN, AttributeValue.newBuilder()
            .setStringValue(
                TruncatableString.newBuilder().setValue(FQN).build())
            .build())
        .build();

    validSpan = Span.newBuilder().setAttributes(attr).build();

    final AttributesReader reader = new AttributesReader(validSpan);

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
    final Span validSpan;

    final Attributes attr = Attributes.newBuilder()
        .putAttributeMap(KEY_LANDSCAPE_TOKEN,
            AttributeValue.newBuilder()
                .setStringValue(TruncatableString.newBuilder().setValue(TOKEN).build()).build())
        .putAttributeMap(KEY_LANDSCAPE_SECRET, AttributeValue.newBuilder()
            .setStringValue(
                TruncatableString.newBuilder().setValue(SECRET).build())
            .build())
        .putAttributeMap(KEY_HOST_NAME, AttributeValue.newBuilder()
            .setStringValue(
                TruncatableString.newBuilder().setValue(HOSTNAME).build())
            .build())
        .putAttributeMap(KEY_HOST_IP, AttributeValue.newBuilder()
            .setStringValue(
                TruncatableString.newBuilder().setValue(HOST_IP).build())
            .build())
        .putAttributeMap(KEY_APPLICATION_INSTANCE_ID,
            AttributeValue.newBuilder()
                .setStringValue(TruncatableString.newBuilder()
                    .setValue(APP_INSTANCE_ID).build())
                .build())
        .putAttributeMap(KEY_APPLICATION_LANGUAGE, AttributeValue.newBuilder()
            .setStringValue(
                TruncatableString.newBuilder().setValue(APP_LANG).build())
            .build())
        .putAttributeMap(KEY_METHOD_FQN, AttributeValue.newBuilder()
            .setStringValue(
                TruncatableString.newBuilder().setValue(FQN).build())
            .build())
        .build();

    validSpan = Span.newBuilder().setAttributes(attr).build();

    final AttributesReader reader = new AttributesReader(validSpan);

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
    final Span validSpan;

    final Attributes attr = Attributes.newBuilder()
        .putAttributeMap(KEY_LANDSCAPE_TOKEN,
            AttributeValue.newBuilder()
                .setStringValue(TruncatableString.newBuilder().setValue(TOKEN).build()).build())
        .putAttributeMap(KEY_LANDSCAPE_SECRET, AttributeValue.newBuilder()
            .setStringValue(
                TruncatableString.newBuilder().setValue(SECRET).build())
            .build())
        .putAttributeMap(KEY_HOST_NAME, AttributeValue.newBuilder()
            .setStringValue(
                TruncatableString.newBuilder().setValue(HOSTNAME).build())
            .build())
        .putAttributeMap(KEY_HOST_IP, AttributeValue.newBuilder()
            .setStringValue(
                TruncatableString.newBuilder().setValue(HOST_IP).build())
            .build())
        .putAttributeMap(KEY_APPLICATION_NAME, AttributeValue.newBuilder()
            .setStringValue(
                TruncatableString.newBuilder().setValue(APP_NAME).build())
            .build())
        .putAttributeMap(KEY_APPLICATION_LANGUAGE, AttributeValue.newBuilder()
            .setStringValue(
                TruncatableString.newBuilder().setValue(APP_LANG).build())
            .build())
        .putAttributeMap(KEY_METHOD_FQN, AttributeValue.newBuilder()
            .setStringValue(
                TruncatableString.newBuilder().setValue(FQN).build())
            .build())
        .build();

    validSpan = Span.newBuilder().setAttributes(attr).build();

    final AttributesReader reader = new AttributesReader(validSpan);

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
    final Span validSpan;

    final Attributes attr = Attributes.newBuilder()
        .putAttributeMap(KEY_LANDSCAPE_TOKEN,
            AttributeValue.newBuilder()
                .setStringValue(TruncatableString.newBuilder().setValue(TOKEN).build()).build())
        .putAttributeMap(KEY_LANDSCAPE_SECRET, AttributeValue.newBuilder()
            .setStringValue(
                TruncatableString.newBuilder().setValue(SECRET).build())
            .build())
        .putAttributeMap(KEY_HOST_NAME, AttributeValue.newBuilder()
            .setStringValue(
                TruncatableString.newBuilder().setValue(HOSTNAME).build())
            .build())
        .putAttributeMap(KEY_HOST_IP, AttributeValue.newBuilder()
            .setStringValue(
                TruncatableString.newBuilder().setValue(HOST_IP).build())
            .build())
        .putAttributeMap(KEY_APPLICATION_NAME, AttributeValue.newBuilder()
            .setStringValue(
                TruncatableString.newBuilder().setValue(APP_NAME).build())
            .build())
        .putAttributeMap(KEY_APPLICATION_INSTANCE_ID,
            AttributeValue.newBuilder()
                .setStringValue(TruncatableString.newBuilder()
                    .setValue(APP_INSTANCE_ID).build())
                .build())
        .putAttributeMap(KEY_METHOD_FQN, AttributeValue.newBuilder()
            .setStringValue(
                TruncatableString.newBuilder().setValue(FQN).build())
            .build())
        .build();

    validSpan = Span.newBuilder().setAttributes(attr).build();

    final AttributesReader reader = new AttributesReader(validSpan);

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
    final Span validSpan;

    final Attributes attr = Attributes.newBuilder()
        .putAttributeMap(KEY_LANDSCAPE_TOKEN,
            AttributeValue.newBuilder()
                .setStringValue(TruncatableString.newBuilder().setValue(TOKEN).build()).build())
        .putAttributeMap(KEY_LANDSCAPE_SECRET, AttributeValue.newBuilder()
            .setStringValue(
                TruncatableString.newBuilder().setValue(SECRET).build())
            .build())
        .putAttributeMap(KEY_HOST_NAME, AttributeValue.newBuilder()
            .setStringValue(
                TruncatableString.newBuilder().setValue(HOSTNAME).build())
            .build())
        .putAttributeMap(KEY_HOST_IP, AttributeValue.newBuilder()
            .setStringValue(
                TruncatableString.newBuilder().setValue(HOST_IP).build())
            .build())
        .putAttributeMap(KEY_APPLICATION_NAME, AttributeValue.newBuilder()
            .setStringValue(
                TruncatableString.newBuilder().setValue(APP_NAME).build())
            .build())
        .putAttributeMap(KEY_APPLICATION_INSTANCE_ID,
            AttributeValue.newBuilder()
                .setStringValue(TruncatableString.newBuilder()
                    .setValue(APP_INSTANCE_ID).build())
                .build())
        .putAttributeMap(KEY_APPLICATION_LANGUAGE, AttributeValue.newBuilder()
            .setStringValue(
                TruncatableString.newBuilder().setValue(APP_LANG).build())
            .build())
        .build();

    validSpan = Span.newBuilder().setAttributes(attr).build();

    final AttributesReader reader = new AttributesReader(validSpan);

    assertEquals(reader.getLandscapeToken(), TOKEN);
    assertEquals(reader.getSecret(), SECRET);
    assertEquals(reader.getHostName(), HOSTNAME);
    assertEquals(reader.getHostIpAddress(), HOST_IP);
    assertEquals(reader.getApplicationName(), APP_NAME);
    assertEquals(reader.getApplicationInstanceId(), APP_INSTANCE_ID);
    assertEquals(reader.getApplicationLanguage(), APP_LANG);
    assertEquals(reader.getMethodFqn(), DefaultAttributeValues.DEFAULT_FQN);
  }

}
