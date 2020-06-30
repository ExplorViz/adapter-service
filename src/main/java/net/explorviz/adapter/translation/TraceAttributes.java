package net.explorviz.adapter.translation;

/**
 * Contains the names of the
 */
public final class TraceAttributes {

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

}
