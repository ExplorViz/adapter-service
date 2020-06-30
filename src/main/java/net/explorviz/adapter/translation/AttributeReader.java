package net.explorviz.adapter.translation;

import io.opencensus.proto.trace.v1.AttributeValue;
import io.opencensus.proto.trace.v1.Span;
import java.util.Map;
import net.explorviz.avro.EVSpan;

/**
 * Reads the attributes of a {@link Span}.
 */
class AttributeReader {


  private String landscapeToken = "";
  private String hostName = "";
  private String hostIPAddress = "";

  private String applicationName = "";
  private String applicationPID = "";
  private String applicationLanguage = "";
  private String methodFQN = "";

  /**
   * Reads attributes from a span.
   *
   * @param span the span to read attributes out of
   */
  AttributeReader(Span span) {
    final Map<String, AttributeValue> attributes = span.getAttributes().getAttributeMapMap();

    if (attributes.containsKey(TraceAttributes.LANDSCAPE_TOKEN)) {
      landscapeToken = attributes.get(TraceAttributes.LANDSCAPE_TOKEN).getStringValue().getValue();
    }

    if (attributes.containsKey(hostName)) {
      hostName = attributes.get(TraceAttributes.HOST_NAME).getStringValue().getValue();
    }

    if (attributes.containsKey(TraceAttributes.HOST_IP)) {
      hostIPAddress = attributes.get(TraceAttributes.HOST_IP).getStringValue().getValue();
    }

    if (attributes.containsKey(TraceAttributes.APPLICATION_NAME)) {
      applicationName =
          attributes.get(TraceAttributes.APPLICATION_NAME).getStringValue().getValue();
    }

    if (attributes.containsKey(TraceAttributes.APPLICATION_PID)) {
      applicationPID = attributes.get(TraceAttributes.APPLICATION_PID).getStringValue().getValue();
    }

    if (attributes.containsKey(TraceAttributes.APPLICATION_LANGUAGE)) {
      applicationLanguage =
          attributes.get(TraceAttributes.APPLICATION_LANGUAGE).getStringValue().getValue();
    }
    if (attributes.containsKey(TraceAttributes.METHOD_FQN)) {
      methodFQN = attributes.get(TraceAttributes.METHOD_FQN).getStringValue().getValue();
    }
  }

  /**
   * Appends all attributes to the given EVSpan builder.
   *
   * @param builder the builder to append the attributes to
   */
  public void append(EVSpan.Builder builder) {
    builder
        .setLandscapeToken(landscapeToken)
        .setHostname(hostName)
        .setHostIpAddress(hostIPAddress)
        .setAppPid(applicationPID)
        .setAppName(applicationName)
        .setAppLanguage(applicationLanguage)
        .setOperationName(methodFQN);
  }

}
