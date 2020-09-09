package net.explorviz.adapter.validation;

import net.explorviz.avro.SpanStructure;
import net.explorviz.avro.Timestamp;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DefaultValueSanitizerTest {


  private DefaultValueSanitizer sanitizer;

  private SpanStructure validSpan;

  @BeforeEach
  void setUp() {
    this.sanitizer = new DefaultValueSanitizer();
    final Instant now = Instant.now();
    final String token = "tok";
    final String hostname = "Host";
    final String hostIp = "1.2.3.4";
    final String appName = "Test App";
    final String appPid = "1234";
    final String appLang = "java";

    this.validSpan = SpanStructure
        .newBuilder()
        .setSpanId("id")
        .setLandscapeToken(token)
        .setHashCode("0bfb1aebe1c7c7b2efd200fb6761d3742f00b34fb337b8532d945d82912b7106")
        .setTimestamp(new Timestamp(now.getEpochSecond(), now.getNano()))
        .setHostname(hostname)
        .setHostIpAddress(hostIp)
        .setAppName(appName)
        .setAppPid(appPid)
        .setAppLanguage(appLang)
        .setFullyQualifiedOperationName("foo.bar.TestClass.testMethod()")
        .build();
  }


  @Test
  void validSpanStructure() {
    SpanStructure got = sanitizer.sanitize(validSpan);
    assertEquals(validSpan, got, "Manipulated a valid span");
  }

  @Test
  void missingHostIpAddress() {
    SpanStructure missingHost = SpanStructure.newBuilder(validSpan).setHostIpAddress("").build();
    SpanStructure got = sanitizer.sanitize(missingHost);
    assertEquals(DefaultValueSanitizer.DEFAULT_HOST_IP, got.getHostIpAddress(),
        "Host IP was not sanitized");
  }

  @Test
  void missingHostName() {
    SpanStructure missingHostname = SpanStructure.newBuilder(validSpan).setHostname("").build();
    SpanStructure got = sanitizer.sanitize(missingHostname);
    assertEquals(DefaultValueSanitizer.DEFAULT_HOST_NAME, got.getHostname(),
        "Hostname was not sanitized");
  }

  @Test
  void missingAppPid() {
    SpanStructure missingPid = SpanStructure.newBuilder(validSpan).setAppPid("").build();
    SpanStructure got = sanitizer.sanitize(missingPid);
    assertEquals(DefaultValueSanitizer.DEFAULT_APP_PID, got.getAppPid(),
        "Application PID was not sanitized");
  }

  @Test
  void missingAppName() {
    SpanStructure missingAppName = SpanStructure.newBuilder(validSpan).setAppName("").build();
    SpanStructure got = sanitizer.sanitize(missingAppName);
    assertEquals(DefaultValueSanitizer.DEFAULT_APP_NAME, got.getAppName(),
        "Application name was not sanitized");
  }

  @Test
  void missingAppLanguage() {
    SpanStructure missingAppLang = SpanStructure.newBuilder(validSpan).setAppLanguage("").build();
    SpanStructure got = sanitizer.sanitize(missingAppLang);
    assertEquals(DefaultValueSanitizer.DEFAULT_APP_LANG, got.getAppLanguage(),
        "Application language was not sanitized");
  }

  @Test
  void missingFQN() {
    SpanStructure missingFQN = SpanStructure.newBuilder(validSpan).setFullyQualifiedOperationName("").build();
    SpanStructure got = sanitizer.sanitize(missingFQN);
    assertEquals(DefaultValueSanitizer.DEFAULT_FQN, got.getFullyQualifiedOperationName(),
        "FQN was not sanitized");
  }

}
