package net.explorviz.adapter.service.converter;

import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

public class DefaultAttributeValueTest {

  @Test
  void testDefaultTokenValue() {
    assertTrue(DefaultAttributeValues.DEFAULT_LANDSCAPE_TOKEN.equals(""));
  }

  @Test
  void testDefaultTokenSecret() {
    assertTrue(DefaultAttributeValues.DEFAULT_LANDSCAPE_SECRET.equals(""));
  }

  @Test
  void testDefaultHostIp() {
    assertTrue(DefaultAttributeValues.DEFAULT_HOST_IP.equals("0.0.0.0")); // NOPMD
  }

  @Test
  void testDefaultHostName() {
    assertTrue(DefaultAttributeValues.DEFAULT_HOST_NAME.equals("UNKNOWN-HOST"));
  }

  @Test
  void testDefaultAppName() {
    assertTrue(DefaultAttributeValues.DEFAULT_APP_NAME.equals("UNKNOWN-APPLICATION"));
  }

  @Test
  void testDefaultInstanceId() {
    assertTrue(DefaultAttributeValues.DEFAULT_APP_INSTANCE_ID.equals("default"));
  }


  @Test
  void testDefaultAppLang() {
    assertTrue(DefaultAttributeValues.DEFAULT_APP_LANG.equals("UNKNOWN"));
  }

  @Test
  void testDefaultFqn() {
    // This must adhere to the format <pkg.Class.method>, i.e., include at least two '.'
    assertTrue(DefaultAttributeValues.DEFAULT_FQN.equals("unknownpkg.UnknownClass.unknownMethod"));
  }

}
