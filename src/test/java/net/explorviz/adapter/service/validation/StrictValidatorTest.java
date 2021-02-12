package net.explorviz.adapter.service.validation;

import java.time.Instant;
import net.explorviz.adapter.service.TokenService;
import net.explorviz.avro.SpanStructure;
import net.explorviz.avro.Timestamp;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;

class StrictValidatorTest {

  private StrictValidator validator;
  private SpanStructure validSpan;

  @BeforeEach
  void setUp() {
    final TokenService mockTokenService = Mockito.mock(TokenService.class);
    Mockito.when(mockTokenService.exists(Matchers.anyString())).thenReturn(true);
    this.validator = new StrictValidator(mockTokenService);

    final Instant now = Instant.now();
    final String token = "tok";
    final String hostname = "Host";
    final String hostIp = "1.2.3.4";
    final String appName = "Test App";
    final String appInstanceId = "1234L";
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
        .setAppInstanceId(appInstanceId)
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
    final SpanStructure noToken =
        SpanStructure.newBuilder(this.validSpan).setLandscapeToken("").build();
    final SpanStructure blankToken =
        SpanStructure.newBuilder(this.validSpan).setLandscapeToken("   ").build();

    for (final SpanStructure tt : new SpanStructure[] {noToken, blankToken}) {
      Assertions.assertFalse(this.validator.isValid(tt));
    }
  }

  @Test
  void invalidTimestamp() {
    final SpanStructure negative =
        SpanStructure.newBuilder(this.validSpan).setTimestamp(new Timestamp(-1L, 0)).build();
    final SpanStructure overflow =
        SpanStructure.newBuilder(this.validSpan)
            .setTimestamp(new Timestamp(1000000000000000000L, 0))
            .build();

    for (final SpanStructure tt : new SpanStructure[] {negative, overflow}) {
      Assertions.assertFalse(this.validator.isValid(tt));
    }
  }

  @Test
  void invalidHost() {
    final SpanStructure noHostname =
        SpanStructure.newBuilder(this.validSpan).setHostname(" ").build();
    final SpanStructure noIpAddress =
        SpanStructure.newBuilder(this.validSpan).setHostIpAddress("\t").build();


    for (final SpanStructure tt : new SpanStructure[] {noHostname, noIpAddress}) {
      Assertions.assertFalse(this.validator.isValid(tt));
    }
  }


  @Test
  void invalidApp() {
    final SpanStructure noName = SpanStructure.newBuilder(this.validSpan).setAppName(" ").build();
    final SpanStructure noLanguage =
        SpanStructure.newBuilder(this.validSpan).setAppLanguage(" ").build();

    for (final SpanStructure tt : new SpanStructure[] {noName, noLanguage}) {
      Assertions.assertFalse(this.validator.isValid(tt));
    }
  }

  @Test
  void invalidOperation() {
    final String noMethod = "foo.Class";
    final String noClass = "foo";
    final String endingDot = "foo.bar.";

    for (final String tt : new String[] {noMethod, noClass, endingDot}) {
      final SpanStructure testee =
          SpanStructure.newBuilder(this.validSpan).setFullyQualifiedOperationName(tt).build();
      Assertions.assertFalse(this.validator.isValid(testee));
    }
  }
}
