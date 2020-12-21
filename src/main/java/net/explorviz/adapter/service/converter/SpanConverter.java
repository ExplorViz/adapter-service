package net.explorviz.adapter.service.converter;

import io.opencensus.proto.trace.v1.Span;

/**
 * Converts OpenCensus spans into another type.
 * @param <T> the type spans are converted to.
 */
public interface SpanConverter<T> {

  /**
   * Converts an OpenCensus {@link Span} into {@link T}.
   */
  T fromOpenCensusSpan(Span ocSpan);

}
