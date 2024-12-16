package net.explorviz.adapter.service.converter;

import org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

class DefaultAttributeValueTest {

  @Test
  fun testDefaultTokenValue() {
    assertEquals(DefaultAttributeValues.DEFAULT_LANDSCAPE_TOKEN, "mytokenvalue")
  }

  @Test
  fun testDefaultTokenSecret() {
    assertEquals(DefaultAttributeValues.DEFAULT_LANDSCAPE_SECRET, "mytokensecret")
  }

  @Test
  fun testDefaultHostIp() {
    assertEquals(DefaultAttributeValues.DEFAULT_HOST_IP, "0.0.0.0")
  }

  @Test
  fun testDefaultHostName() {
    assertEquals(DefaultAttributeValues.DEFAULT_HOST_NAME, "UNKNOWN-HOST")
  }

  @Test
  fun testDefaultAppName() {
    assertEquals(DefaultAttributeValues.DEFAULT_APP_NAME, "UNKNOWN-APPLICATION")
  }

  @Test
  fun testDefaultInstanceId() {
    assertEquals(DefaultAttributeValues.DEFAULT_APP_INSTANCE_ID, "0")
  }

  @Test
  fun testDefaultAppLang() {
    assertEquals(DefaultAttributeValues.DEFAULT_APP_LANG, "UNKNOWN")
  }

  @Test
  fun testDefaultFqn() {
    // This must adhere to the format <pkg.Class.method>, i.e., include at least two '.'
    assertEquals(DefaultAttributeValues.DEFAULT_FQN, "unknownpkg.UnknownClass.unknownMethod")
  }
}
