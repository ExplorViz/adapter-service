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

    this.validSpan = Mockito.mock(AttributesReader.class);
    Mockito.when(this.validSpan.getSecret()).thenReturn(secret);
    Mockito.when(this.validSpan.getLandscapeToken()).thenReturn(token);
    Mockito.when(this.validSpan.getHostName()).thenReturn(hostname);
    Mockito.when(this.validSpan.getHostIpAddress()).thenReturn(hostIp);
    Mockito.when(this.validSpan.getApplicationName()).thenReturn(appName);
    Mockito.when(this.validSpan.getApplicationInstanceId()).thenReturn(appInstanceId);
    Mockito.when(this.validSpan.getApplicationLanguage()).thenReturn(appLang);
    Mockito.when(this.validSpan.getMethodFqn()).thenReturn(fqn);

  }

  @Test
  void valid() {
    Assertions.assertTrue(this.validator.isValid(this.validSpan));
  }

  @Test
  void invalidToken() {
    for (final String invalid : new String[] {"", "   ", null}) {
      Mockito.when(this.validSpan.getLandscapeToken()).thenReturn(invalid);
      Assertions.assertFalse(this.validator.isValid(this.validSpan));
    }
  }



  @Test
  void invalidHost() {

    for (final String invalidHost : new String[] {"", "\n", "\t", "   ", null}) {
      for (final String invalidIp : new String[] {"", "\t", "\n", "   ", null}) {
        Mockito.when(this.validSpan.getHostIpAddress()).thenReturn(invalidHost);
        Mockito.when(this.validSpan.getHostIpAddress()).thenReturn(invalidIp);
        Assertions.assertFalse(this.validator.isValid(this.validSpan));
      }
    }
  }


  @Test
  void invalidApp() {
    for (final String invalidLang : new String[] {"", "\n", "\t", "   ", null}) {
      for (final String invalidName : new String[] {"", "\t", "\n", "   ", null}) {
        Mockito.when(this.validSpan.getApplicationLanguage()).thenReturn(invalidLang);
        Mockito.when(this.validSpan.getApplicationName()).thenReturn(invalidName);
        Assertions.assertFalse(this.validator.isValid(this.validSpan));
      }
    }
  }

  @Test
  void invalidOperation() {
    final String noMethod = "foo.Class";
    final String noClass = "foo";
    final String endingDot = "foo.bar.";

    for (final String tt : new String[] {noMethod, noClass, endingDot}) {
      Mockito.when(this.validSpan.getMethodFqn()).thenReturn(tt);
      Assertions.assertFalse(this.validator.isValid(this.validSpan));
    }
  }
}
