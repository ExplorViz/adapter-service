package net.explorviz.adapter.translation;

import com.google.common.io.BaseEncoding;
import io.opencensus.proto.trace.v1.Span;
import java.time.Duration;
import java.time.Instant;
import javax.enterprise.context.ApplicationScoped;
import net.explorviz.avro.EVSpan;
import net.explorviz.avro.Timestamp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Converts {@link Span}s to {@link EVSpan}s.
 */
@ApplicationScoped
public class SpanConverter {

  private static final Logger LOGGER = LoggerFactory.getLogger(SpanConverter.class);

  private static final int TRACE_ID_LEN = 16;
  private static final int SPAN_ID_LEN = 8;

  /**
   * Converts a {@link Span} to an {@link EVSpan}
   *
   * @param original the original span
   * @return the converted span
   */
  public EVSpan toEVSpan(Span original) {

    final String traceId = BaseEncoding.base16().lowerCase()
        .encode(original.getTraceId().toByteArray(), 0, TRACE_ID_LEN);

    final String spanId = BaseEncoding.base16().lowerCase()
        .encode(original.getSpanId().toByteArray(), 0, SPAN_ID_LEN);

    final Timestamp startTime =
        new Timestamp(original.getStartTime().getSeconds(), original.getStartTime().getNanos());

    final long endTime = Instant
        .ofEpochSecond(original.getEndTime().getSeconds(), original.getEndTime().getNanos())
        .toEpochMilli();

    final long duration =
        endTime - Duration.ofSeconds(startTime.getSeconds(), startTime.getNanoAdjust()).toMillis();

    final EVSpan.Builder builder = EVSpan.newBuilder();
    builder.setRequestCount(1)
        .setSpanId(spanId)
        .setTraceId(traceId)
        .setStartTime(startTime)
        .setEndTime(endTime)
        .setDuration(duration);

    AttributeReader attributeReader = new AttributeReader(original);
    attributeReader.append(builder);


    EVSpan span = builder.build();

    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("Converted EVSpan: {}", span.toString());
    }

    return span;
  }

}
