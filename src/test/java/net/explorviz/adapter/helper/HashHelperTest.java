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
    final String appInstancedId = "1234L";
    final String fqn = "foo.bar.TestClass.testMethod()";

    final AttributesReader attr = Mockito.mock(AttributesReader.class);
    Mockito.when(attr.getLandscapeToken()).thenReturn(token);
    Mockito.when(attr.getHostIpAddress()).thenReturn(hostIp);
    Mockito.when(attr.getApplicationInstanceId()).thenReturn(appInstancedId);
    Mockito.when(attr.getMethodFqn()).thenReturn(fqn);


    final String expectedValue = "bc2527c89c57dbd9e0e6528335a338ba389a1fef97fd1f15e2ab6134c27341f2";
    assertEquals(expectedValue, HashHelper.fromSpanAttributes(attr), "Hashes are not equal");
  }


}
