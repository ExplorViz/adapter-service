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
  AttributeReader(final Span span) {
    final Map<String, AttributeValue> attributes = span.getAttributes().getAttributeMapMap();

    if (attributes.containsKey(TraceAttributes.LANDSCAPE_TOKEN)) {
      this.landscapeToken =
          attributes.get(TraceAttributes.LANDSCAPE_TOKEN).getStringValue().getValue();
    }

    if (attributes.containsKey(TraceAttributes.HOST_NAME)) {
      this.hostName = attributes.get(TraceAttributes.HOST_NAME).getStringValue().getValue();
    }

    if (attributes.containsKey(TraceAttributes.HOST_IP)) {
      this.hostIPAddress = attributes.get(TraceAttributes.HOST_IP).getStringValue().getValue();
    }

    if (attributes.containsKey(TraceAttributes.APPLICATION_NAME)) {
      this.applicationName =
          attributes.get(TraceAttributes.APPLICATION_NAME).getStringValue().getValue();
    }

    if (attributes.containsKey(TraceAttributes.APPLICATION_PID)) {
      this.applicationPID =
          attributes.get(TraceAttributes.APPLICATION_PID).getStringValue().getValue();
    }

    if (attributes.containsKey(TraceAttributes.APPLICATION_LANGUAGE)) {
      this.applicationLanguage =
          attributes.get(TraceAttributes.APPLICATION_LANGUAGE).getStringValue().getValue();
    }
    if (attributes.containsKey(TraceAttributes.METHOD_FQN)) {
      this.methodFQN = attributes.get(TraceAttributes.METHOD_FQN).getStringValue().getValue();
    }
  }

  /**
   * Appends all attributes to the given EVSpan builder.
   *
   * @param builder the builder to append the attributes to
   */
  public void append(final EVSpan.Builder builder) {
    builder
        .setLandscapeToken(this.landscapeToken)
        .setHostname(this.hostName)
        .setHostIpAddress(this.hostIPAddress)
        .setAppPid(this.applicationPID)
        .setAppName(this.applicationName)
        .setAppLanguage(this.applicationLanguage)
        .setFullyQualifiedOperationName(this.methodFQN);
  }

}
