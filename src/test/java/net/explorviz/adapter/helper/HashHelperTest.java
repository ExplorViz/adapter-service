package net.explorviz.adapter.helper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.time.Instant;
import net.explorviz.avro.EVSpan;
import net.explorviz.avro.Timestamp;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class HashHelperTest {

  private EVSpan validSpan;

  @BeforeEach
  void setUp() {

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
        .setHashCode("")
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
  void testHashFunction() {
    final String expectedValue = "429756767f5de088856ba6d2dbbb973ee7d740b75e5cfb9cfc60610e07941136";
    assertEquals(expectedValue, HashHelper.spanToHexHashString(validSpan), "Hashes are not equal");
  }


}
