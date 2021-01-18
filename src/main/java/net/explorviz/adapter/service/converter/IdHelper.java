package net.explorviz.adapter.service.converter;

import com.google.common.io.BaseEncoding;

/**
 * Helper class to convert IDs given as byte arrays to strings.
 */
public final class IdHelper {

  private static final int SPAN_ID_LEN = 8;
  private static final int TRACE_ID_LEN = 16;

  /**
   * Converts a span ID represented a byte string to a readable string encoded in base 16.
   *
   * @param spanId the ID
   * @return base16 encoding of the ID
   */
  public static String converterSpanId(final byte[] spanId) {
    return BaseEncoding.base16().lowerCase().encode(spanId, 0, SPAN_ID_LEN);
  }

  /**
   * Converts a trace ID represented a byte string to a readable string encoded in base 16.
   *
   * @param traceId the ID
   * @return base16 encoding of the ID
   */
  public static String converterTraceId(final byte[] traceId) {
    return BaseEncoding.base16().lowerCase().encode(traceId, 0, TRACE_ID_LEN);
  }

}
