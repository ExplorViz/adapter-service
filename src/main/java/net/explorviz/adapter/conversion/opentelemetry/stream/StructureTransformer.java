package net.explorviz.adapter.conversion.opentelemetry.stream;


import io.opentelemetry.proto.trace.v1.Span;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import net.explorviz.adapter.conversion.opentelemetry.converter.OtSpanStructureConverter;
import net.explorviz.adapter.util.PerfomanceLogger;
import net.explorviz.adapter.validation.SpanStructureSanitizer;
import net.explorviz.avro.SpanStructure;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.Transformer;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class StructureTransformer implements Transformer<byte[], Span, KeyValue<String, SpanStructure>> {

  private final static Logger LOGGER = LoggerFactory.getLogger(StructureTransformer.class);
  private PerfomanceLogger perfLogger =
      PerfomanceLogger.newOperationPerformanceLogger(LOGGER, 1000, "Converted {} spans in {} ms");


  private final SpanStructureSanitizer sanitizer;

  private final OtSpanStructureConverter converter;

  @Inject
  public StructureTransformer(final SpanStructureSanitizer sanitizer,
                              final OtSpanStructureConverter converter) {
    this.sanitizer = sanitizer;
    this.converter = converter;
  }

  @Override
  public void init(final ProcessorContext context) {

  }

  @Override
  public KeyValue<String, SpanStructure> transform(final byte[] key, final Span s) {
    SpanStructure span = this.converter.toSpanStructure(s);
    span = sanitizer.sanitize(span);
    perfLogger.logOperation();
    return new KeyValue<>(span.getLandscapeToken(), span);
  }

  @Override
  public void close() {

  }
}
