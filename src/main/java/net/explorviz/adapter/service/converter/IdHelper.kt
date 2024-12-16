package net.explorviz.adapter.service.converter;

import com.google.common.io.BaseEncoding;

/**
 * Helper class to convert IDs given as byte arrays to strings.
 */
object IdHelper {

  private const val SPAN_ID_LEN = 8
  private const val TRACE_ID_LEN = 16

  /**
   * Converts a span ID represented as a byte array to a readable string encoded in base 16.
   *
   * @param spanId the ID
   * @return base16 encoding of the ID
   */
  fun convertSpanId(spanId: ByteArray): String {
    return BaseEncoding.base16().lowerCase().encode(spanId, 0, SPAN_ID_LEN)
  }

  /**
   * Converts a trace ID represented as a byte array to a readable string encoded in base 16.
   *
   * @param traceId the ID
   * @return base16 encoding of the ID
   */
  fun convertTraceId(traceId: ByteArray): String {
    return BaseEncoding.base16().lowerCase().encode(traceId, 0, TRACE_ID_LEN)
  }
}

