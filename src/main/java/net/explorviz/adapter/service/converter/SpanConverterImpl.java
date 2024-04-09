package net.explorviz.adapter.service.converter;

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

    String parentSpan = "";
    if (ocSpan.getParentSpanId().size() > 0) {
      parentSpan = IdHelper.converterSpanId(ocSpan.getParentSpanId().toByteArray());
    }

    final Span.Builder span = Span.newBuilder()
        .setLandscapeToken(attributesReader.getLandscapeToken())
        .setGitCommitChecksum(attributesReader.getGitCommitChecksum())
        .setParentSpanId(parentSpan)
        .setSpanId(IdHelper.converterSpanId(ocSpan.getSpanId().toByteArray()))
        .setTraceId(IdHelper.converterTraceId(ocSpan.getTraceId().toByteArray()))
        .setStartTimeEpochMilli(ocSpan.getStartTimeUnixNano() / TO_MILLISEC_DIVISOR)
        .setEndTimeEpochMilli(ocSpan.getEndTimeUnixNano() / TO_MILLISEC_DIVISOR);

    attributesReader.appendToSpan(span);

    return span.build();
  }
}
