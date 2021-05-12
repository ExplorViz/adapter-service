package net.explorviz.adapter.service.validation;

import java.util.Optional;
import net.explorviz.adapter.service.TokenService;
import net.explorviz.adapter.service.converter.AttributesReader;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;

class StrictValidatorTest {

  private StrictValidator validator;
  private AttributesReader validSpan;

  @BeforeEach
  void setUp() {
    final TokenService mockTokenService = Mockito.mock(TokenService.class);
    Mockito.when(mockTokenService.exists(Matchers.anyString())).thenReturn(true);
    this.validator = new StrictValidator(mockTokenService);


    final String token = "tok";
    final String secret = "secret";
    final String hostname = "Host";
    final String hostIp = "1.2.3.4";
    final String appName = "Test App";
    final String appInstanceId = "1234L";
    final String appLang = "java";
    final String fqn = "foo.bar.test()";
    Mockito.when(mockTokenService.getSecret(Matchers.eq(token))).thenReturn(Optional.of(token));

    validSpan = Mockito.mock(AttributesReader.class);
    Mockito.when(validSpan.getSecret()).thenReturn(secret);
    Mockito.when(validSpan.getLandscapeToken()).thenReturn(token);
    Mockito.when(validSpan.getHostName()).thenReturn(hostname);
    Mockito.when(validSpan.getHostIpAddress()).thenReturn(hostIp);
    Mockito.when(validSpan.getApplicationName()).thenReturn(appName);
    Mockito.when(validSpan.getApplicationInstanceId()).thenReturn(appInstanceId);
    Mockito.when(validSpan.getApplicationLanguage()).thenReturn(appLang);
    Mockito.when(validSpan.getMethodFqn()).thenReturn(fqn);

  }

  @Test
  void valid() {
    Assertions.assertTrue(this.validator.isValid(this.validSpan));
  }

  @Test
  void invalidToken() {
    for (String invalid : new String[] {"", "   ", null}) {
      Mockito.when(validSpan.getLandscapeToken()).thenReturn(invalid);
      Assertions.assertFalse(this.validator.isValid(validSpan));
    }
  }



  @Test
  void invalidHost() {

    for (String invalidHost : new String[] {"", "\n", "\t", "   ", null}) {
      for (String invalidIp : new String[] {"", "\t", "\n", "   ", null}) {
        Mockito.when(validSpan.getHostIpAddress()).thenReturn(invalidHost);
        Mockito.when(validSpan.getHostIpAddress()).thenReturn(invalidIp);
        Assertions.assertFalse(this.validator.isValid(validSpan));
      }
    }
  }


  @Test
  void invalidApp() {
    for (String invalidLang : new String[] {"", "\n", "\t", "   ", null}) {
      for (String invalidName : new String[] {"", "\t", "\n", "   ", null}) {
        Mockito.when(validSpan.getApplicationLanguage()).thenReturn(invalidLang);
        Mockito.when(validSpan.getApplicationName()).thenReturn(invalidName);
        Assertions.assertFalse(this.validator.isValid(validSpan));
      }
    }
  }

  @Test
  void invalidOperation() {
    final String noMethod = "foo.Class";
    final String noClass = "foo";
    final String endingDot = "foo.bar.";

    for (final String tt : new String[] {noMethod, noClass, endingDot}) {
      Mockito.when(validSpan.getMethodFqn()).thenReturn(tt);
      Assertions.assertFalse(this.validator.isValid(validSpan));
    }
  }
}
