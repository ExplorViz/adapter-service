package net.explorviz.adapter.service.converter;

import static net.explorviz.adapter.service.converter.DefaultAttributeValues.DEFAULT_APP_INSTANCE_ID;
import static net.explorviz.adapter.service.converter.DefaultAttributeValues.DEFAULT_APP_LANG;
import static net.explorviz.adapter.service.converter.DefaultAttributeValues.DEFAULT_APP_NAME;
import static net.explorviz.adapter.service.converter.DefaultAttributeValues.DEFAULT_CLASS_FQN;
import static net.explorviz.adapter.service.converter.DefaultAttributeValues.DEFAULT_HOST_IP;
import static net.explorviz.adapter.service.converter.DefaultAttributeValues.DEFAULT_HOST_NAME;
import static net.explorviz.adapter.service.converter.DefaultAttributeValues.DEFAULT_LANDSCAPE_SECRET;
import static net.explorviz.adapter.service.converter.DefaultAttributeValues.DEFAULT_LANDSCAPE_TOKEN;
import static net.explorviz.adapter.service.converter.DefaultAttributeValues.DEFAULT_METHOD;

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
   * The programming language that the application is written in.
   */
  public static final String APPLICATION_LANGUAGE = "application_language";

  /**
   * The identifier of the operation/method called.
   */
  public static final String CODE_FUNCTION = "code.function";

  /**
   * The fully qualified class name to which the called operation/method belongs.
   */
  public static final String CODE_NAMESPACE = "code.namespace";

  /**
   * The fully qualified name of the operation/method called.
   */
  public static final String METHOD_FQN = "java.fqn";

  /*
   * Default values
   */

  // k8s section

  public static final String K8S_POD_NAME = "k8s.pod.name";
  public static final String K8S_NAMESPACE_NAME = "k8s.namespace.name";
  public static final String K8S_NODE_NAME = "k8s.node.name";
  public static final String K8S_DEPLOYMENT_NAME = "k8s.deployment.name";


  private final Map<String, AnyValue> attributes = new HashMap<>(7);

  private Span span = null;

  /**
   * Reads attributes from a span.
   *
   * @param span the span to read attributes out of
   */
  public AttributesReader(final Span span) {
    this.span = span;

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
    String fallbackMethodName = this.span.getName();
    if (fallbackMethodName.isEmpty()) {
      fallbackMethodName = DEFAULT_METHOD;
    }

    Optional<String> codeNamespace = this.getAsString(CODE_NAMESPACE);
    Optional<String> codeFunction = this.getAsString(CODE_FUNCTION);
    Optional<String> methodFqn = this.getAsString(METHOD_FQN);

    return codeNamespace.flatMap(classFqn -> codeFunction.map(methodName -> classFqn + "." + methodName))
        .or(() -> methodFqn)
        .orElse(DEFAULT_CLASS_FQN + fallbackMethodName);
  }

  public String getK8sPodName() {
    return this.getAsString(K8S_POD_NAME).orElse("");
  }

  public String getK8sNamespace() {
    return this.getAsString(K8S_NAMESPACE_NAME).orElse("");
  }

  public String getK8sNodeName() {
    return this.getAsString(K8S_NODE_NAME).orElse("");
  }

  public String getK8sDeploymentName() {
    return this.getAsString(K8S_DEPLOYMENT_NAME).orElse("");
  }


  /**
   * Appends all attributes to the given {{@link net.explorviz.avro.Span}} builder.
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
        .setFullyQualifiedOperationName(this.getMethodFqn())
        .setK8sPodName(this.getK8sPodName())
        .setK8sNamespace(this.getK8sNamespace())
        .setK8sNodeName(this.getK8sNodeName())
        .setK8sDeploymentName(this.getK8sDeploymentName());
  }
}
