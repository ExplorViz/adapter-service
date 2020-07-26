package net.explorviz.adapter.translation;

import io.opencensus.proto.trace.v1.AttributeValue;
import io.opencensus.proto.trace.v1.Span;
import java.util.Map;
import net.explorviz.avro.SpanStructure;

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

    if (attributes.containsKey(SpanAttributes.LANDSCAPE_TOKEN)) {
      this.landscapeToken =
          attributes.get(SpanAttributes.LANDSCAPE_TOKEN).getStringValue().getValue();
    }

    if (attributes.containsKey(SpanAttributes.HOST_NAME)) {
      this.hostName = attributes.get(SpanAttributes.HOST_NAME).getStringValue().getValue();
    }

    if (attributes.containsKey(SpanAttributes.HOST_IP)) {
      this.hostIPAddress = attributes.get(SpanAttributes.HOST_IP).getStringValue().getValue();
    }

    if (attributes.containsKey(SpanAttributes.APPLICATION_NAME)) {
      this.applicationName =
          attributes.get(SpanAttributes.APPLICATION_NAME).getStringValue().getValue();
    }

    if (attributes.containsKey(SpanAttributes.APPLICATION_PID)) {
      this.applicationPID =
          attributes.get(SpanAttributes.APPLICATION_PID).getStringValue().getValue();
    }

    if (attributes.containsKey(SpanAttributes.APPLICATION_LANGUAGE)) {
      this.applicationLanguage =
          attributes.get(SpanAttributes.APPLICATION_LANGUAGE).getStringValue().getValue();
    }
    if (attributes.containsKey(SpanAttributes.METHOD_FQN)) {
      this.methodFQN = attributes.get(SpanAttributes.METHOD_FQN).getStringValue().getValue();
    }
  }

  /**
   * Appends all attributes to the given SpanStructure builder.
   *
   * @param builder the builder to append the attributes to
   */
  public void append(final SpanStructure.Builder builder) {
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
