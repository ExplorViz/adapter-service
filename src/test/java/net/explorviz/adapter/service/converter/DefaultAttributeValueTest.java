package net.explorviz.adapter.service.converter;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class DefaultAttributeValueTest {

  @Test
  void testDefaultTokenValue() {
    assertEquals(DefaultAttributeValues.DEFAULT_LANDSCAPE_TOKEN, "mytokenvalue");
  }

  @Test
  void testDefaultTokenSecret() {
    assertEquals(DefaultAttributeValues.DEFAULT_LANDSCAPE_SECRET, "mytokensecret");
  }

  @Test
  void testDefaultHostIp() {
    assertEquals(DefaultAttributeValues.DEFAULT_HOST_IP, "0.0.0.0");
  }

  @Test
  void testDefaultHostName() {
    assertEquals(DefaultAttributeValues.DEFAULT_HOST_NAME, "UNKNOWN-HOST");
  }

  @Test
  void testDefaultAppName() {
    assertEquals(DefaultAttributeValues.DEFAULT_APP_NAME, "UNKNOWN-APPLICATION");
  }

  @Test
  void testDefaultInstanceId() {
    assertEquals(DefaultAttributeValues.DEFAULT_APP_INSTANCE_ID, "0");
  }


  @Test
  void testDefaultAppLang() {
    assertEquals(DefaultAttributeValues.DEFAULT_APP_LANG, "UNKNOWN");
  }

  @Test
  void testDefaultFqn() {
    // This must adhere to the format <pkg.Class.method>, i.e., include at least two '.'
    assertEquals(DefaultAttributeValues.DEFAULT_FQN, "unknownpkg.UnknownClass.unknownMethod");
  }

}
