package net.explorviz.adapter.service.converter;

import static net.explorviz.adapter.service.converter.DefaultAttributeValues.DEFAULT_FQN;
import static net.explorviz.adapter.service.converter.DefaultAttributeValues.JS_FQN;

import jakarta.enterprise.context.ApplicationScoped;
import net.explorviz.avro.Span;
import java.util.Objects;

/**
 * Converts a {@link io.opentelemetry.proto.trace.v1.Span} to a {@link Span}.
 */
@ApplicationScoped
public class SpanConverterImpl implements SpanConverter<Span> {

  private static final long TO_MILLISEC_DIVISOR = 1_000_000L;


  @Override
  public Span fromOpenTelemetrySpan(final io.opentelemetry.proto.trace.v1.Span ocSpan) {

    final AttributesReader attributesReader = new AttributesReader(ocSpan);

    // if Fqn = default value, we set the method name to spanID, otherwise do not change it
    String methodName;
    if (Objects.equals(attributesReader.getMethodFqn(), DEFAULT_FQN)) {
      methodName = JS_FQN + IdHelper.converterSpanId(ocSpan.getSpanId().toByteArray());
    }
    else {
      methodName = attributesReader.getMethodFqn();
    }

    String parentSpan = "";
    if (ocSpan.getParentSpanId().size() > 0) {
      parentSpan = IdHelper.converterSpanId(ocSpan.getParentSpanId().toByteArray());
    }

    final Span.Builder span = Span.newBuilder()
        .setLandscapeToken(attributesReader.getLandscapeToken())
        .setParentSpanId(parentSpan)
        .setSpanId(IdHelper.converterSpanId(ocSpan.getSpanId().toByteArray()))
        .setTraceId(IdHelper.converterTraceId(ocSpan.getTraceId().toByteArray()))
        .setFullyQualifiedOperationName(methodName)
        .setStartTimeEpochMilli(ocSpan.getStartTimeUnixNano() / TO_MILLISEC_DIVISOR)
        .setEndTimeEpochMilli(ocSpan.getEndTimeUnixNano() / TO_MILLISEC_DIVISOR);

    attributesReader.appendToSpan(span);

    //System.out.print(" FQN in Attributes reader: " + attributesReader.getMethodFqn());
    //System.out.print(" FQN in span: " + span.getFullyQualifiedOperationName());

    return span.build();
  }
}
