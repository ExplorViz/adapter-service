package net.explorviz.adapter.helper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import net.explorviz.adapter.service.converter.AttributesReader;
import net.explorviz.adapter.service.converter.HashHelper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class HashHelperTest {

  @Test
  void testHashFunction() {
    final String token = "tok";
    final String hostIp = "1.2.3.4";
    final String appPid = "1234";
    final String fqn = "foo.bar.TestClass.testMethod()";

    final AttributesReader attr = Mockito.mock(AttributesReader.class);
    Mockito.when(attr.getLandscapeToken()).thenReturn(token);
    Mockito.when(attr.getHostIpAddress()).thenReturn(hostIp);
    Mockito.when(attr.getApplicationPid()).thenReturn(appPid);
    Mockito.when(attr.getMethodFqn()).thenReturn(fqn);


    final String expectedValue = "429756767f5de088856ba6d2dbbb973ee7d740b75e5cfb9cfc60610e07941136";
    assertEquals(expectedValue, HashHelper.fromSpanAttributes(attr), "Hashes are not equal");
  }


}
