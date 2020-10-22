package net.explorviz.adapter.translation;

/**
 * Contains constants for missing attribute values of spans.
 */
public final class DefaultAttributeValues {

  public static final String DEFAULT_HOST_IP = "0.0.0.0";
  public static final String DEFAULT_HOST_NAME = "UNKNOWN-HOST";
  public static final String DEFAULT_APP_PID = "-1";
  public static final String DEFAULT_APP_NAME = "UNKNOWN-APPLICATION";
  public static final String DEFAULT_APP_LANG = "UNKNOWN";
  // This must adhere to the format <pkg.Class.method>, i.e., include at least two '.'
  public static final String DEFAULT_FQN = "unknownpkg.UnknownClass.unknownMethod";

}
