package net.explorviz.adapter.service.converter;

import static net.explorviz.adapter.service.converter.DefaultAttributeValues.DEFAULT_APP_LANG;
import static net.explorviz.adapter.service.converter.DefaultAttributeValues.DEFAULT_APP_NAME;
import static net.explorviz.adapter.service.converter.DefaultAttributeValues.DEFAULT_APP_PID;
import static net.explorviz.adapter.service.converter.DefaultAttributeValues.DEFAULT_FQN;
import static net.explorviz.adapter.service.converter.DefaultAttributeValues.DEFAULT_HOST_IP;
import static net.explorviz.adapter.service.converter.DefaultAttributeValues.DEFAULT_HOST_NAME;
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
   * The PID of the applicatino a span belongs to.
   */
  public static final String APPLICATION_PID = "application_pid";

  /**
   * The PID of the applicatino a span belongs to.
   */
  public static final String APPLICATION_LANGUAGE = "application_language";

  /**
   * The fully qualified name of the operation/method called.
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
  public AttributesReader(final Span span) {
    span.getAttributes().getAttributeMapMap().forEach((k, v) -> {
      this.attributes.put(k, v.getStringValue().getValue());
    });
  }

  public String getLandscapeToken() {
    return this.attributes.getOrDefault(LANDSCAPE_TOKEN, "");
  }

  public String getHostName() {
    return this.attributes.getOrDefault(HOST_NAME, DEFAULT_HOST_NAME);
  }

  public String getHostIpAddress() {
    return this.attributes.getOrDefault(HOST_IP, DEFAULT_HOST_IP);
  }

  public String getApplicationName() {
    return this.attributes.getOrDefault(APPLICATION_NAME, DEFAULT_APP_NAME);
  }

  public String getApplicationPid() {
    return this.attributes.getOrDefault(APPLICATION_PID, DEFAULT_APP_PID);
  }

  public String getApplicationLanguage() {
    return this.attributes.getOrDefault(APPLICATION_LANGUAGE, DEFAULT_APP_LANG);
  }

  public String getMethodFqn() {
    return this.attributes.getOrDefault(METHOD_FQN, DEFAULT_FQN);
  }

  /**
   * Appends all attributes to the given SpanStructure builder.
   *
   * @param builder the builder to append the attributes to
   */
  public void appendToStructure(final SpanStructure.Builder builder) {
    builder
        .setLandscapeToken(this.getLandscapeToken())
        .setHostname(this.getHostName())
        .setHostIpAddress(this.getHostIpAddress())
        .setAppPid(this.getApplicationPid())
        .setAppName(this.getApplicationName())
        .setAppLanguage(this.getApplicationLanguage())
        .setFullyQualifiedOperationName(this.getMethodFqn());
  }

}
