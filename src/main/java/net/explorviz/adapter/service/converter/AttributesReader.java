package net.explorviz.adapter.service.converter;

import static net.explorviz.adapter.service.converter.DefaultAttributeValues.DEFAULT_APP_INSTANCE_ID;
import static net.explorviz.adapter.service.converter.DefaultAttributeValues.DEFAULT_APP_LANG;
import static net.explorviz.adapter.service.converter.DefaultAttributeValues.DEFAULT_APP_NAME;
import static net.explorviz.adapter.service.converter.DefaultAttributeValues.DEFAULT_FQN;
import static net.explorviz.adapter.service.converter.DefaultAttributeValues.DEFAULT_HOST_IP;
import static net.explorviz.adapter.service.converter.DefaultAttributeValues.DEFAULT_HOST_NAME;
import static net.explorviz.adapter.service.converter.DefaultAttributeValues.DEFAULT_LANDSCAPE_SECRET;
import static net.explorviz.adapter.service.converter.DefaultAttributeValues.DEFAULT_LANDSCAPE_TOKEN;

import io.opentelemetry.proto.common.v1.AnyValue;
import io.opentelemetry.proto.trace.v1.Span;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Reads the attributes of a {@link Span}.
 */
public class AttributesReader {

  /**
   * The token that uniquely identifies the landscape a span belongs to.
   */
  public static final String LANDSCAPE_TOKEN = "landscape_token";

  /**
   * The token's secret.
   */
  public static final String TOKEN_SECRET = "token_secret";

  /**
   * The name of the host.
   */
  public static final String HOST_NAME = "host";

  /**
   * The IP address the application runs on.
   */
  public static final String HOST_IP = "host_address";

  /**
   * The name of the application a span belongs to.
   */
  public static final String APPLICATION_NAME = "application_name";

  /**
   * The instance id of the application.
   */
  public static final String APPLICATION_INSTANCE_ID = "application_instance_id";

  /**
   * The PID of the applicatino a span belongs to.
   */
  public static final String APPLICATION_LANGUAGE = "application_language";

  /**
   * The fully qualified name of the operation/method called.
   */
  public static final String METHOD_FQN = "java.fqn";

  /*
   * Default values
   */

  private final Map<String, AnyValue> attributes = new HashMap<>(7);

  /**
   * Reads attributes from a span.
   *
   * @param span the span to read attributes out of
   */
  public AttributesReader(final Span span) {
    // Load attributes into map
    span.getAttributesList().forEach(keyValue -> {
      attributes.put(keyValue.getKey(), keyValue.getValue());
    });
  }

  /**
   * Unwraps an AttributeValue of into a string.
   *
   * @param key the attribute's key
   * @return the string value of the attribute or empty if no such key exists
   */
  private Optional<String> getAsString(final String key) {
    final AnyValue av = this.attributes.get(key);
    if (av == null) {
      return Optional.empty();
    }

    return Optional.of(av.getStringValue());
  }

  public String getLandscapeToken() {
    return this.getAsString(LANDSCAPE_TOKEN).orElse(DEFAULT_LANDSCAPE_TOKEN);
  }

  public String getSecret() {
    return this.getAsString(TOKEN_SECRET).orElse(DEFAULT_LANDSCAPE_SECRET);
  }

  public String getHostName() {
    return this.getAsString(HOST_NAME).orElse(DEFAULT_HOST_NAME);
  }

  public String getHostIpAddress() {
    return this.getAsString(HOST_IP).orElse(DEFAULT_HOST_IP);
  }

  public String getApplicationName() {
    return this.getAsString(APPLICATION_NAME).orElse(DEFAULT_APP_NAME);
  }

  public String getApplicationInstanceId() {
    return this.getAsString(APPLICATION_INSTANCE_ID).orElse(DEFAULT_APP_INSTANCE_ID);
  }

  public String getApplicationLanguage() {
    return this.getAsString(APPLICATION_LANGUAGE).orElse(DEFAULT_APP_LANG);
  }

  public String getMethodFqn() {
    return this.getAsString(METHOD_FQN).orElse(DEFAULT_FQN);
  }

  /**
   * Appends all attributes to the given {{@link net.explorviz.avro.Span}}
   * builder. The fqn holds an
   * exception, since we want to process the span names as method names and
   * therefore we set the fqn
   * to the one we set in the {{@link net.explorviz.avro.Span}} converter.
   *
   * @param builder the builder to append the attributes to
   */
  public void appendToSpan(final net.explorviz.avro.Span.Builder builder) {
    builder
        .setLandscapeToken(this.getLandscapeToken())
        .setHostname(this.getHostName())
        .setHostIpAddress(this.getHostIpAddress())
        .setAppInstanceId(this.getApplicationInstanceId())
        .setAppName(this.getApplicationName())
        .setAppLanguage(this.getApplicationLanguage())
        .setFullyQualifiedOperationName(builder.getFullyQualifiedOperationName());
  }

}
