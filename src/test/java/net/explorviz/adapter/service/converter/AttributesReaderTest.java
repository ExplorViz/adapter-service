package net.explorviz.adapter.service.converter;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.protobuf.InvalidProtocolBufferException;
import io.opencensus.proto.trace.v1.AttributeValue;
import io.opencensus.proto.trace.v1.Span;
import io.opencensus.proto.trace.v1.Span.Attributes;
import io.opencensus.proto.trace.v1.TruncatableString;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class AttributesReaderTest {

  private static final String LANDSCAPE_TOKEN = AttributesReader.LANDSCAPE_TOKEN;
  private static final String TOKEN_SECRET = AttributesReader.TOKEN_SECRET;
  private static final String HOST_NAME = AttributesReader.HOST_NAME;
  private static final String HOST_IP = AttributesReader.HOST_IP;
  private static final String APPLICATION_NAME = AttributesReader.APPLICATION_NAME;
  private static final String APPLICATION_INSTANCE_ID = AttributesReader.APPLICATION_INSTANCE_ID;
  private static final String APPLICATION_LANGUAGE = AttributesReader.APPLICATION_LANGUAGE;
  private static final String METHOD_FQN = AttributesReader.METHOD_FQN;

  private Span validSpan;

  private final String token = "tok";
  private final String secret = "secret";
  private final String hostname = "Host";
  private final String hostIp = "1.2.3.4";
  private final String appName = "Test App";
  private final String appInstanceId = "1234L";
  private final String appLang = "java";
  private final String fqn = "foo.bar.test()";

  @BeforeEach
  void setUp() throws InvalidProtocolBufferException {



    final Attributes attr = Attributes.newBuilder()
        .putAttributeMap(LANDSCAPE_TOKEN,
            AttributeValue.newBuilder()
                .setStringValue(TruncatableString.newBuilder().setValue(token).build()).build())
        .putAttributeMap(TOKEN_SECRET, AttributeValue.newBuilder()
            .setStringValue(TruncatableString.newBuilder().setValue(secret).build()).build())
        .putAttributeMap(HOST_NAME, AttributeValue.newBuilder()
            .setStringValue(TruncatableString.newBuilder().setValue(hostname).build()).build())
        .putAttributeMap(HOST_IP, AttributeValue.newBuilder()
            .setStringValue(TruncatableString.newBuilder().setValue(hostIp).build()).build())
        .putAttributeMap(APPLICATION_NAME, AttributeValue.newBuilder()
            .setStringValue(TruncatableString.newBuilder().setValue(appName).build()).build())
        .putAttributeMap(APPLICATION_INSTANCE_ID,
            AttributeValue.newBuilder()
                .setStringValue(TruncatableString.newBuilder().setValue(appInstanceId).build())
                .build())
        .putAttributeMap(APPLICATION_LANGUAGE, AttributeValue.newBuilder()
            .setStringValue(TruncatableString.newBuilder().setValue(appLang).build()).build())
        .putAttributeMap(METHOD_FQN, AttributeValue.newBuilder()
            .setStringValue(TruncatableString.newBuilder().setValue(fqn).build()).build())
        .build();

    this.validSpan = Span.newBuilder().setAttributes(attr).build();

  }

  @Test
  void testValidSpanReadOut() {
    final AttributesReader reader = new AttributesReader(this.validSpan);

    assertEquals(reader.getLandscapeToken(), token);
    assertEquals(reader.getSecret(), secret);
    assertEquals(reader.getHostName(), hostname);
    assertEquals(reader.getHostIpAddress(), hostIp);
    assertEquals(reader.getApplicationName(), appName);
    assertEquals(reader.getApplicationInstanceId(), appInstanceId);
    assertEquals(reader.getApplicationLanguage(), appLang);
    assertEquals(reader.getMethodFqn(), fqn);

  }

}
