package net.explorviz.adapter.translation;

import com.google.common.io.BaseEncoding;
import io.opencensus.proto.trace.v1.Span;
import java.security.NoSuchAlgorithmException;
import javax.enterprise.context.ApplicationScoped;
import net.explorviz.adapter.helper.HashHelper;
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

  private static final int SPAN_ID_LEN = 8;

  /**
   * Converts a {@link Span} to an {@link EVSpan}
   *
   * @param original the original span
   * @return the converted span
   * @throws NoSuchAlgorithmException
   */
  public EVSpan toEVSpan(final Span original) {

    final String spanId = BaseEncoding.base16().lowerCase()
        .encode(original.getSpanId().toByteArray(), 0, SPAN_ID_LEN);

    final Timestamp startTime =
        new Timestamp(original.getStartTime().getSeconds(), original.getStartTime().getNanos());

    final EVSpan.Builder builder = EVSpan.newBuilder();
    builder
        .setSpanId(spanId)
        .setTimestamp(startTime);

    final AttributeReader attributeReader = new AttributeReader(original);
    attributeReader.append(builder);

    // temporary hash code since the field is required for avro builder
    builder.setHashCode("");

    final EVSpan span = builder.build();

    // HashCode is used to map structural and dynamic data
    final String hashCode = HashHelper.spanToHexHashString(span);
    span.setHashCode(hashCode);

    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("Converted EVSpan: {}", span.toString());
    }

    return span;
  }

}
