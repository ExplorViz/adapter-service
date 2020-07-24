package net.explorviz.adapter.validation;

import java.time.Instant;
import net.explorviz.avro.EVSpan;
import net.explorviz.avro.Timestamp;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class StrictValidatorTest {

  private StrictValidator validator;
  private EVSpan validSpan;

  @BeforeEach
  void setUp() {
    this.validator = new StrictValidator();

    final Instant now = Instant.now();
    final String token = "tok";
    final String hostname = "Host";
    final String hostIp = "1.2.3.4";
    final String appName = "Test App";
    final String appPid = "1234";
    final String appLang = "java";

    this.validSpan = EVSpan
        .newBuilder()
        .setSpanId("id")
        .setLandscapeToken(token)
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
  void valid() {
    Assertions.assertTrue(this.validator.isValid(this.validSpan));
  }

  @Test
  void invalidToken() {
    final EVSpan noToken = EVSpan.newBuilder(this.validSpan).setLandscapeToken("").build();
    final EVSpan blankToken = EVSpan.newBuilder(this.validSpan).setLandscapeToken("   ").build();

    for (final EVSpan tt : new EVSpan[] {noToken, blankToken}) {
      Assertions.assertFalse(this.validator.isValid(tt));
    }
  }

  @Test
  void invalidTimestamp() {
    final EVSpan negative =
        EVSpan.newBuilder(this.validSpan).setTimestamp(new Timestamp(-1L, 0)).build();
    final EVSpan overflow =
        EVSpan.newBuilder(this.validSpan).setTimestamp(new Timestamp(1000000000000000000L, 0))
            .build();

    for (final EVSpan tt : new EVSpan[] {negative, overflow}) {
      Assertions.assertFalse(this.validator.isValid(tt));
    }
  }

  @Test
  void invalidHost() {
    final EVSpan noHostname = EVSpan.newBuilder(this.validSpan).setHostname(" ").build();
    final EVSpan noIpAddress = EVSpan.newBuilder(this.validSpan).setHostIpAddress("\t").build();


    for (final EVSpan tt : new EVSpan[] {noHostname, noIpAddress}) {
      Assertions.assertFalse(this.validator.isValid(tt));
    }
  }


  @Test
  void invalidApp() {
    final EVSpan noName = EVSpan.newBuilder(this.validSpan).setAppName(" ").build();
    final EVSpan noLanguage = EVSpan.newBuilder(this.validSpan).setAppLanguage(" ").build();
    final EVSpan noPid = EVSpan.newBuilder(this.validSpan).setAppPid(" ").build();

    for (final EVSpan tt : new EVSpan[] {noName, noLanguage, noPid}) {
      Assertions.assertFalse(this.validator.isValid(tt));
    }
  }

  @Test
  void invalidOperation() {
    final String noMethod = "foo.Class";
    final String noClass = "foo";
    final String endingDot = "foo.bar.";

    for (final String tt : new String[] {noMethod, noClass, endingDot}) {
      final EVSpan testee =
          EVSpan.newBuilder(this.validSpan).setFullyQualifiedOperationName(tt).build();
      Assertions.assertFalse(this.validator.isValid(testee));
    }
  }
}
