package net.explorviz.adapter.service.converter;



import static net.explorviz.adapter.service.converter.DefaultAttributeValues.*;

import io.opencensus.proto.trace.v1.Span;
import java.util.HashMap;
import java.util.Map;
import net.explorviz.avro.SpanStructure;

/**
 * Reads the attributes of a {@link Span}.
 */
public class AttributesReader {

  /**
   * The token that uniquely identifies the landscape a span belongs to.
   */
  public static final String LANDSCAPE_TOKEN = "landscape_token";

  /**
   * The name of the host.
   */
  public static final String HOST_NAME = "host_name";

  /**
   * The IP address the application runs on.
   */
  public static final String HOST_IP = "host_ip";

  /**
   * The name of the application a span belongs to.
   */
  public static final String APPLICATION_NAME = "application_name";

  /**
   * The PID of the applicatino a span belongs to
   */
  public static final String APPLICATION_PID = "application_pid";

  /**
   * The PID of the applicatino a span belongs to
   */
  public static final String APPLICATION_LANGUAGE = "application_language";

  /**
   * The fully qualified name of the operation/method called
   */
  public static final String METHOD_FQN = "method_fqn";

  /*
   * Default values
   */



  private final Map<String, String> attributes = new HashMap<>(7);

  /**
   * Reads attributes from a span.
   *
   * @param span the span to read attributes out of
   */
  AttributesReader(final Span span) {
    span.getAttributes().getAttributeMapMap().forEach((k, v) -> {
      attributes.put(k, v.getStringValue().getValue());
    });
  }

  public String getLandscapeToken() {
    return attributes.getOrDefault(LANDSCAPE_TOKEN, "");
  }

  public String getHostName() {
    return attributes.getOrDefault(HOST_NAME, DEFAULT_HOST_NAME);
  }

  public String getHostIPAddress() {
    return attributes.getOrDefault(HOST_IP, DEFAULT_HOST_IP);
  }

  public String getApplicationName() {
    return attributes.getOrDefault(APPLICATION_NAME, DEFAULT_APP_NAME);
  }

  public String getApplicationPID() {
    return attributes.getOrDefault(APPLICATION_PID, DEFAULT_APP_PID);
  }

  public String getApplicationLanguage() {
    return attributes.getOrDefault(APPLICATION_LANGUAGE, DEFAULT_APP_LANG);
  }

  public String getMethodFQN() {
    return attributes.getOrDefault(METHOD_FQN, DEFAULT_FQN);
  }

  /**
   * Appends all attributes to the given SpanStructure builder.
   *
   * @param builder the builder to append the attributes to
   */
  public void appendToStructure(final SpanStructure.Builder builder) {
    builder
        .setLandscapeToken(getLandscapeToken())
        .setHostname(getHostName())
        .setHostIpAddress(getHostIPAddress())
        .setAppPid(getApplicationPID())
        .setAppName(getApplicationName())
        .setAppLanguage(getApplicationLanguage())
        .setFullyQualifiedOperationName(getMethodFQN());
  }

}
