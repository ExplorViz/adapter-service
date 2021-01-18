package net.explorviz.adapter.service.converter;

import io.opencensus.proto.trace.v1.Span;
import javax.enterprise.context.ApplicationScoped;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.explorviz.avro.SpanDynamic;
import net.explorviz.avro.Timestamp;

/**
 * Converts a {@link Span} to a {@link SpanDynamic}.
 */
@ApplicationScoped
public class SpanDynamicConverter implements SpanConverter<SpanDynamic> {

  private static final Logger LOGGER = LoggerFactory.getLogger(SpanDynamicConverter.class);

  @Override
  public SpanDynamic fromOpenCensusSpan(final Span ocSpan) {
    final Timestamp startTime =
        new Timestamp(ocSpan.getStartTime().getSeconds(), ocSpan.getStartTime().getNanos());

    final Timestamp endTime =
        new Timestamp(ocSpan.getEndTime().getSeconds(), ocSpan.getEndTime().getNanos());

    AttributesReader attributesReader = new AttributesReader(ocSpan);


    String parentSpan = "";
    if (ocSpan.getParentSpanId().size() > 0) {
      parentSpan = IdHelper.converterSpanId(ocSpan.getParentSpanId().toByteArray());
    }

    SpanDynamic spanDynamic = SpanDynamic.newBuilder()
        .setLandscapeToken(attributesReader.getLandscapeToken())
        .setParentSpanId(parentSpan)
        .setSpanId(IdHelper.converterSpanId(ocSpan.getSpanId().toByteArray()))
        .setTraceId(IdHelper.converterTraceId(ocSpan.getTraceId().toByteArray()))
        .setHashCode("") // temporary hash code since the field is required for avro builder
        .setStartTime(startTime)
        .setEndTime(endTime)
        .build();

    String hashValue = HashHelper.fromSpanAttributes(new AttributesReader(ocSpan));

    spanDynamic.setHashCode(hashValue);

    if (LOGGER.isTraceEnabled()) {
      LOGGER.trace("Converted SpanDynamic: {}", spanDynamic.toString());
    }

    return spanDynamic;
  }
}
