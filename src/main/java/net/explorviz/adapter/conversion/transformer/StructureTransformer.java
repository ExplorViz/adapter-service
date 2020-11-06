package net.explorviz.adapter.conversion.transformer;

import io.opencensus.proto.trace.v1.Span;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import net.explorviz.adapter.service.converter.SpanStructureConverter;
import net.explorviz.adapter.service.validation.SpanStructureSanitizer;
import net.explorviz.avro.SpanStructure;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.Transformer;
import org.apache.kafka.streams.processor.ProcessorContext;

@ApplicationScoped
public class StructureTransformer implements Transformer<byte[], Span, KeyValue<String, SpanStructure>> {


  private final SpanStructureSanitizer sanitizer;

  private final SpanStructureConverter converter;

  @Inject
  public StructureTransformer(final SpanStructureSanitizer sanitizer,
                              final SpanStructureConverter converter) {
    this.sanitizer = sanitizer;
    this.converter = converter;
  }

  @Override
  public void init(final ProcessorContext context) {

  }

  @Override
  public KeyValue<String, SpanStructure> transform(final byte[] key, final Span s) {
    SpanStructure span = this.converter.fromOpenCensusSpan(s);
    span = sanitizer.sanitize(span);
    return new KeyValue<>(span.getLandscapeToken(), span);
  }

  @Override
  public void close() {

  }
}
