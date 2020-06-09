package net.explorviz.adapter.validation;

import net.explorviz.avro.EVSpan;
import net.explorviz.avro.Timestamp;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.wildfly.common.Assert;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class StrictValidatorTest {

  private StrictValidator validator;
  private EVSpan validSpan;

  @BeforeEach
  void setUp() {
    validator = new StrictValidator();

    Instant now = Instant.now();
    long duration = 1000L;
    long end = now.toEpochMilli() + duration;
    String token = "tok";
    String hostname = "Host";
    String hostIp = "1.2.3.4";
    String appName = "Test App";
    String appPid = "1234";
    String appLang = "java";

    validSpan = EVSpan
        .newBuilder()
        .setTraceId("trace")
        .setRequestCount(12)
        .setSpanId("id")
        .setLandscapeToken(token)
        .setStartTime(new Timestamp(now.getEpochSecond(), now.getNano()))
        .setEndTime(end)
        .setDuration(duration)
        .setHostname(hostname)
        .setHostIpAddress(hostIp)
        .setAppName(appName)
        .setAppPid(appPid)
        .setAppLanguage(appLang)
        .setOperationName("foo.bar.TestClass.testMethod()")
        .build();

  }

  @Test
  void valid() {
    Assertions.assertDoesNotThrow(() -> validator.validate(validSpan));
    Assertions.assertTrue(validator.isValid(validSpan));
  }

  @Test
  void invalidToken() {
    EVSpan noToken = EVSpan.newBuilder(validSpan).setLandscapeToken("").build();
    EVSpan blankToken = EVSpan.newBuilder(validSpan).setLandscapeToken("   ").build();

    for (EVSpan tt : new EVSpan[] {noToken, blankToken}) {
      Assertions.assertThrows(InvalidSpanException.class, () -> validator.validate(tt));
    }
  }

  @Test
  void invalidTimestamp() {
    EVSpan negative = EVSpan.newBuilder(validSpan).setStartTime(new Timestamp(-1L, 0)).build();
    EVSpan overflow =
        EVSpan.newBuilder(validSpan).setStartTime(new Timestamp(1000000000000000000L, 0)).build();

    for (EVSpan tt : new EVSpan[] {negative, overflow}) {
      Assertions.assertThrows(InvalidSpanException.class, () -> validator.validate(tt));
    }
  }

  @Test
  void invalidHost() {
    EVSpan noHostname = EVSpan.newBuilder(validSpan).setHostname(" ").build();
    EVSpan noIpAddress = EVSpan.newBuilder(validSpan).setHostIpAddress("\t").build();


    for (EVSpan tt : new EVSpan[] {noHostname, noIpAddress}) {
      Assertions.assertThrows(InvalidSpanException.class, () -> validator.validate(tt));
    }
  }


  @Test
  void invalidApp() {
    EVSpan noName = EVSpan.newBuilder(validSpan).setAppName(" ").build();
    EVSpan noLanguage = EVSpan.newBuilder(validSpan).setAppLanguage(" ").build();
    EVSpan noPid = EVSpan.newBuilder(validSpan).setAppPid(" ").build();

    for (EVSpan tt : new EVSpan[] {noName, noLanguage, noPid}) {
      Assertions.assertThrows(InvalidSpanException.class, () -> validator.validate(tt));
    }
  }

  @Test
  void invalidOperation() {
    String noMethod = "foo.Class";
    String noClass = "foo";
    String endingDot = "foo.bar.";

    for (String tt : new String[] {noMethod, noClass, endingDot}) {
      EVSpan testee = EVSpan.newBuilder(validSpan).setOperationName(tt).build();
      Assertions.assertThrows(InvalidSpanException.class, () -> validator.validate(testee), tt);
    }
  }
}
