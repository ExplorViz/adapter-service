package net.explorviz.adapter.service.converter;

import static net.explorviz.adapter.service.converter.DefaultAttributeValues.DEFAULT_FQN;

import jakarta.enterprise.context.ApplicationScoped;
import net.explorviz.avro.Span;

/**
 * Converts a {@link io.opentelemetry.proto.trace.v1.Span} to a {@link Span}.
 */
@ApplicationScoped
public class SpanConverterImpl implements SpanConverter<Span> {

  private static final long TO_MILLISEC_DIVISOR = 1_000_000L;


  @Override
  public Span fromOpenTelemetrySpan(final io.opentelemetry.proto.trace.v1.Span ocSpan) {

    final AttributesReader attributesReader = new AttributesReader(ocSpan);

    String methodName = "";
    if (attributesReader.getMethodFqn() == DEFAULT_FQN) {
      methodName = IdHelper.converterSpanId(ocSpan.getSpanId().toByteArray()) + "." + IdHelper.converterSpanId(ocSpan.getSpanId().toByteArray())
      + "." + IdHelper.converterSpanId(ocSpan.getSpanId().toByteArray());
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

    System.out.print("SpanID: " + IdHelper.converterSpanId(ocSpan.getSpanId().toByteArray()));
    System.out.print("FQN: " + span.getFullyQualifiedOperationName());

    attributesReader.appendToSpan(span);

    return span.build();
  }
}
