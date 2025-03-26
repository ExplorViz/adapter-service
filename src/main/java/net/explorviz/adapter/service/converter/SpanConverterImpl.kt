package net.explorviz.adapter.service.converter

import jakarta.enterprise.context.ApplicationScoped
import net.explorviz.avro.Span

/** Converts a [io.opentelemetry.proto.trace.v1.Span] to a [Span]. */
@ApplicationScoped
class SpanConverterImpl : SpanConverter<Span> {
    override fun fromOpenTelemetrySpan(ocSpan: io.opentelemetry.proto.trace.v1.Span): Span {

        val attributesReader = AttributesReader(ocSpan)

        val parentSpan =
            if (ocSpan.parentSpanId.size() > 0) {
                IdHelper.convertSpanId(ocSpan.parentSpanId.toByteArray())
            } else {
                ""
            }

        val span =
            Span.newBuilder()
                .setLandscapeToken(attributesReader.landscapeToken)
                .setGitCommitChecksum(attributesReader.gitCommitChecksum)
                .setParentSpanId(parentSpan)
                .setSpanId(IdHelper.convertSpanId(ocSpan.spanId.toByteArray()))
                .setTraceId(IdHelper.convertTraceId(ocSpan.traceId.toByteArray()))
                .setStartTimeEpochMilli(ocSpan.startTimeUnixNano)
                .setEndTimeEpochMilli(ocSpan.endTimeUnixNano)

        attributesReader.appendToSpan(span)

        return span.build()
    }
}
