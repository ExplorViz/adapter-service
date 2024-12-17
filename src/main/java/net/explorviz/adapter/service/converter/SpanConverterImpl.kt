package net.explorviz.adapter.service.converter

import jakarta.enterprise.context.ApplicationScoped
import net.explorviz.avro.Span

/** Converts a [io.opentelemetry.proto.trace.v1.Span] to a [Span]. */
@ApplicationScoped
class SpanConverterImpl : SpanConverter<Span> {

    private companion object {
        const val TO_MILLISEC_DIVISOR = 1_000_000L
    }

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
                .setStartTimeEpochMilli(ocSpan.startTimeUnixNano / TO_MILLISEC_DIVISOR)
                .setEndTimeEpochMilli(ocSpan.endTimeUnixNano / TO_MILLISEC_DIVISOR)

        attributesReader.appendToSpan(span)

        return span.build()
    }
}
