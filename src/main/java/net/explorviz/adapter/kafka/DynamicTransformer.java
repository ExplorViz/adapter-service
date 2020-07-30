package net.explorviz.adapter.kafka;

import io.opencensus.proto.trace.v1.Span;
import net.explorviz.avro.SpanStructure;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.Transformer;
import org.apache.kafka.streams.processor.ProcessorContext;

public class DynamicTransformer
    implements Transformer<byte[], Span, KeyValue<String, SpanStructure>> {
  @Override
  public void init(final ProcessorContext context) {

  }

  @Override
  public KeyValue<String, SpanStructure> transform(final byte[] key, final Span value) {
    return null;
  }

  @Override
  public void close() {

  }
}
