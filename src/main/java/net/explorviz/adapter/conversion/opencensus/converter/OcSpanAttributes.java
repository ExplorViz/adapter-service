package net.explorviz.adapter.conversion.opencensus.converter;

import io.opencensus.proto.trace.v1.AttributeValue;
import io.opencensus.proto.trace.v1.Span;
import java.util.Map;
import net.explorviz.adapter.conversion.SpanAttributes;
import net.explorviz.avro.SpanStructure;

/**
 * Reads the attributes of a {@link Span}.
 */
public class OcSpanAttributes implements SpanAttributes {

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


  private final Map<String, AttributeValue> attributes;

  /**
   * Reads attributes from a span.
   *
   * @param span the span to read attributes out of
   */
  OcSpanAttributes(final Span span) {
    this.attributes = span.getAttributes().getAttributeMapMap();
  }

  public String getLandscapeToken() {
    String token = null;
    if (attributes.containsKey(LANDSCAPE_TOKEN)) {
      token =  attributes.get(LANDSCAPE_TOKEN).getStringValue().getValue();
    }
    return token;
  }

  public String getHostName() {
    String hostName = null;
    if (attributes.containsKey(HOST_NAME)) {
      hostName = attributes.get(HOST_NAME).getStringValue().getValue();
    }
    return hostName;
  }

  public String getHostIPAddress() {
    String hostIP = null;
    if (attributes.containsKey(HOST_IP)) {
      hostIP = attributes.get(HOST_IP).getStringValue().getValue();
    }
    return hostIP;
  }

  public String getApplicationName() {
    String appName = null;
    if (attributes.containsKey(APPLICATION_NAME)) {
      appName = attributes.get(APPLICATION_NAME).getStringValue().getValue();
    }
    return appName;
  }

  public String getApplicationPID() {
    String appPid = null;
    if (attributes.containsKey(APPLICATION_PID)) {
      appPid = attributes.get(APPLICATION_PID).getStringValue().getValue();
    }
    return appPid;
  }

  public String getApplicationLanguage() {
    String appLang = null;
    if (attributes.containsKey(APPLICATION_LANGUAGE)) {
      appLang = attributes.get(APPLICATION_LANGUAGE).getStringValue().getValue();
    }
    return appLang;
  }

  public String getMethodFQN() {
    String fqn = null;
    if (attributes.containsKey(METHOD_FQN)) {
      fqn = attributes.get(METHOD_FQN).getStringValue().getValue();
    }
    return fqn;
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
