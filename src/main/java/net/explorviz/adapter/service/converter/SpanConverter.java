package net.explorviz.adapter.service.converter;

import io.opentelemetry.proto.trace.v1.Span;

/**
 * Converts OpenCensus spans into another type.
 *
 * @param <T> the type spans are converted to.
 */
public interface SpanConverter<T> {

  /**
   * Converts an OpenCensus {@link Span} into {@link T}.
   */
  T fromOpenTelemetrySpan(Span ocSpan);

}
