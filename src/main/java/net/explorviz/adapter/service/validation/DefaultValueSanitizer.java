package net.explorviz.adapter.service.validation;

import net.explorviz.avro.SpanStructure;

/**
 * Sanitizes a {@link SpanStructure} element by setting default values for missing attributes, if possible.
 * The following attributes stay untouched:
 * <ul>
 *   <li>The landscape token,</li>
 *   <li>the hash code,</li>
 *   <li>the timestamp, and</li>
 *   <li>the span Id</li>
 * </ul>
 *
 */
public class DefaultValueSanitizer implements SpanStructureSanitizer {

  public static final String DEFAULT_HOST_IP = "0.0.0.0";
  public static final String DEFAULT_HOST_NAME = "UNKNOWN-HOST";

  public static final String DEFAULT_APP_PID = "-1";
  public static final String DEFAULT_APP_NAME = "UNKNOWN-APPLICATION";
  public static final String DEFAULT_APP_LANG = "UNKNOWN";

  // This must adhere to the format <pkg.Class.method>, i.e., include at least two '.'
  public static final String DEFAULT_FQN = "unknownpkg.UnknownClass.unknownMethod";

  @Override
  public SpanStructure sanitize(SpanStructure span) {
    SpanStructure.Builder builder = SpanStructure.newBuilder(span);
    if (isNullOrBlankString(builder.getHostIpAddress())) {
      builder.setHostIpAddress(DEFAULT_HOST_IP);
    }
    if (isNullOrBlankString(builder.getHostname())) {
      builder.setHostname(DEFAULT_HOST_NAME);
    }

    if (isNullOrBlankString(builder.getAppPid())) {
      builder.setAppPid(DEFAULT_APP_PID);
    }
    if (isNullOrBlankString(builder.getAppName())) {
      builder.setAppName(DEFAULT_APP_NAME);
    }
    if (isNullOrBlankString(builder.getAppLanguage())) {
      builder.setAppLanguage(DEFAULT_APP_LANG);
    }

    if (isNullOrBlankString(builder.getFullyQualifiedOperationName())) {
      builder.setFullyQualifiedOperationName(DEFAULT_FQN);
    }

    return builder.build();
  }


  private boolean isNullOrBlankString(String s) {
    return s == null || s.isBlank();
  }

}
