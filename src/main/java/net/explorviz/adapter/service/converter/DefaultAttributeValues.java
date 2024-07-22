package net.explorviz.adapter.service.converter;

/**
 * Contains constants for missing attribute values of spans.
 */
public final class DefaultAttributeValues {

  public static final String DEFAULT_LANDSCAPE_TOKEN = "mytokenvalue";
  public static final String DEFAULT_LANDSCAPE_SECRET = "mytokensecret";
  public static final String DEFAULT_HOST_IP = "0.0.0.0"; // NOPMD
  public static final String DEFAULT_HOST_NAME = "UNKNOWN-HOST";
  public static final String DEFAULT_APP_NAME = "UNKNOWN-APPLICATION";
  public static final String DEFAULT_APP_INSTANCE_ID = "default";
  public static final String DEFAULT_APP_LANG = "UNKNOWN";
  // FQN must adhere to the format <pkg.Class.method>, i.e., include at least two '.'
  public static final String DEFAULT_CLASS_FQN = "unknownpkg.UnknownClass";
  public static final String DEFAULT_METHOD = "unknownMethod";
  public static final String DEFAULT_FQN = DEFAULT_CLASS_FQN + "." + DEFAULT_METHOD;
}
