package net.explorviz.adapter.service.converter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import com.google.protobuf.InvalidProtocolBufferException;
import io.opencensus.proto.trace.v1.AttributeValue;
import io.opencensus.proto.trace.v1.Span;
import io.opencensus.proto.trace.v1.Span.Attributes;
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

  @BeforeEach
  void setUp() throws InvalidProtocolBufferException {

    final String token = "tok";
    final String secret = "secret";
    final String hostname = "Host";
    final String hostIp = "1.2.3.4";
    final String appName = "Test App";
    final String appInstanceId = "1234L";
    final String appLang = "java";
    final String fqn = "foo.bar.test()";

    final Attributes attr = Attributes.newBuilder()
        .putAttributeMap(LANDSCAPE_TOKEN, AttributeValue.parseFrom(token.getBytes()))
        .putAttributeMap(TOKEN_SECRET, AttributeValue.parseFrom(secret.getBytes()))
        .putAttributeMap(HOST_NAME, AttributeValue.parseFrom(hostname.getBytes()))
        .putAttributeMap(HOST_IP, AttributeValue.parseFrom(hostIp.getBytes()))
        .putAttributeMap(APPLICATION_NAME, AttributeValue.parseFrom(appName.getBytes()))
        .putAttributeMap(APPLICATION_INSTANCE_ID,
            AttributeValue.parseFrom(appInstanceId.getBytes()))
        .putAttributeMap(APPLICATION_LANGUAGE, AttributeValue.parseFrom(appLang.getBytes()))
        .putAttributeMap(METHOD_FQN, AttributeValue.parseFrom(fqn.getBytes()))
        .build();

    this.validSpan = Span.newBuilder().setAttributes(attr).build();

  }

  @Test
  void testValidSpanReadOut() {
    final AttributesReader reader = new AttributesReader(this.validSpan);

    assertEquals(reader.getLandscapeToken(), "tok");
  }

}
