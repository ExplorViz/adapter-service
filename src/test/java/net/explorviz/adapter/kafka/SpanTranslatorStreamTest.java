package net.explorviz.adapter.kafka;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.common.io.BaseEncoding;
import com.google.protobuf.ByteString;
import com.google.protobuf.Timestamp;
import io.confluent.kafka.schemaregistry.client.MockSchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient;
import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import io.opencensus.proto.dump.DumpSpans;
import io.opencensus.proto.trace.v1.AttributeValue;
import io.opencensus.proto.trace.v1.Span;
import io.opencensus.proto.trace.v1.TruncatableString;
import io.quarkus.test.junit.QuarkusTest;
import java.nio.charset.Charset;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import javax.inject.Inject;
import net.explorviz.adapter.translation.SpanConverter;
import net.explorviz.adapter.translation.TraceAttributes;
import net.explorviz.adapter.validation.NoOpSanitizer;
import net.explorviz.adapter.validation.SpanSanitizer;
import net.explorviz.adapter.validation.SpanValidator;
import net.explorviz.adapter.validation.StrictValidator;
import net.explorviz.avro.EVSpan;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.TestInputTopic;
import org.apache.kafka.streams.TestOutputTopic;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.TopologyTestDriver;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@QuarkusTest
class SpanTranslatorStreamTest {

  private TopologyTestDriver driver;

  private TestInputTopic<byte[], byte[]> inputTopic;
  private TestOutputTopic<String, EVSpan> outputTopic;

  private SpecificAvroSerde<EVSpan> evSpanSerDe;

  @Inject
  KafkaConfig config;

  @BeforeEach
  void setUp() {

    final SchemaRegistryClient schemaRegistryClient = new MockSchemaRegistryClient();

    SpanValidator v = new StrictValidator();
    SpanSanitizer s = new NoOpSanitizer();
    SpanConverter c = new SpanConverter();

    final Topology topology =
        new SpanTranslatorStream(schemaRegistryClient, this.config, c, v, s).getTopology();

    final Properties props = new Properties();
    props.put(StreamsConfig.APPLICATION_ID_CONFIG, "test");
    props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "dummy:1234");

    this.driver = new TopologyTestDriver(topology, props);

    this.evSpanSerDe = new SpecificAvroSerde<>(schemaRegistryClient);

    this.evSpanSerDe.configure(
        Map.of(AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, "http://dummy"), false);

    this.inputTopic = this.driver.createInputTopic("cluster-dump-spans",
        Serdes.ByteArray().serializer(), Serdes.ByteArray().serializer());
    this.outputTopic = this.driver.createOutputTopic("explorviz-spans",
        Serdes.String().deserializer(), this.evSpanSerDe.deserializer());
  }

  @AfterEach
  void tearDown() {
    this.evSpanSerDe.close();
    this.driver.close();
  }

  private Span sampleSpan() {

    Map<String, AttributeValue> attrMap = new HashMap<>();
    attrMap.put(TraceAttributes.LANDSCAPE_TOKEN, AttributeValue.newBuilder()
        .setStringValue(TruncatableString.newBuilder().setValue("token")).build());
    attrMap.put(TraceAttributes.HOST_IP, AttributeValue.newBuilder()
        .setStringValue(TruncatableString.newBuilder().setValue("1.2.3.4")).build());
    attrMap.put(TraceAttributes.HOST_NAME, AttributeValue.newBuilder()
        .setStringValue(TruncatableString.newBuilder().setValue("hostname")).build());
    attrMap.put(TraceAttributes.APPLICATION_LANGUAGE, AttributeValue.newBuilder()
        .setStringValue(TruncatableString.newBuilder().setValue("language")).build());
    attrMap.put(TraceAttributes.APPLICATION_NAME, AttributeValue.newBuilder()
        .setStringValue(TruncatableString.newBuilder().setValue("appname")).build());
    attrMap.put(TraceAttributes.APPLICATION_PID, AttributeValue.newBuilder()
        .setStringValue(TruncatableString.newBuilder().setValue("1234")).build());
    attrMap.put(TraceAttributes.METHOD_FQN, AttributeValue.newBuilder()
        .setStringValue(TruncatableString.newBuilder().setValue("net.example.Bar.foo()")).build());


    return Span.newBuilder()
        .setTraceId(
            ByteString.copyFrom("50c246ad9c9883d1558df9f19b9ae7a6", Charset.defaultCharset()))
        .setSpanId(ByteString.copyFrom("7ef83c66eabd5fbb", Charset.defaultCharset()))
        .setStartTime(Timestamp.newBuilder().setSeconds(123).setNanos(456).build())
        .setEndTime(Timestamp.newBuilder().setSeconds(456).setNanos(789).build())
        .setAttributes(Span.Attributes.newBuilder().putAllAttributeMap(attrMap))
        .build();
  }

  @Test
  void testAttributeTranslation() {
    final Span testSpan = sampleSpan();
    final DumpSpans singleSpanDump = DumpSpans.newBuilder().addSpans(testSpan).build();
    this.inputTopic.pipeInput(testSpan.getSpanId().toByteArray(), singleSpanDump.toByteArray());

    final EVSpan result = this.outputTopic.readKeyValue().value;

    Map<String, AttributeValue> attrs = testSpan.getAttributes().getAttributeMapMap();
    final String expectedToken =
        attrs.get(TraceAttributes.LANDSCAPE_TOKEN).getStringValue().getValue();
    final String expectedHostName =
        attrs.get(TraceAttributes.HOST_NAME).getStringValue().getValue();
    final String expectedHostIP = attrs.get(TraceAttributes.HOST_IP).getStringValue().getValue();
    final String expectedAppName =
        attrs.get(TraceAttributes.APPLICATION_NAME).getStringValue().getValue();
    final String expectedAppLang =
        attrs.get(TraceAttributes.APPLICATION_LANGUAGE).getStringValue().getValue();
    final String expectedAppPID =
        attrs.get(TraceAttributes.APPLICATION_PID).getStringValue().getValue();
    final String expectedOperationName =
        attrs.get(TraceAttributes.METHOD_FQN).getStringValue().getValue();


    assertEquals(expectedToken, result.getLandscapeToken(), "Invalid token");

    assertEquals(expectedHostIP, result.getHostIpAddress(), "Invalid host ip address");
    assertEquals(expectedHostName, result.getHostname(), "Invalid host name");

    assertEquals(expectedAppName, result.getAppName(), "Invalid application name");
    assertEquals(expectedAppPID, result.getAppPid(), "Invalid application pid");
    assertEquals(expectedAppLang, result.getAppLanguage(), "Invalid application language");

    assertEquals(expectedOperationName, result.getOperationName(), "Invalid operation name");

  }


  @Test
  void testIdTranslation() {

    final Span testSpan = sampleSpan();
    final DumpSpans singleSpanDump = DumpSpans.newBuilder().addSpans(testSpan).build();
    this.inputTopic.pipeInput(testSpan.getSpanId().toByteArray(), singleSpanDump.toByteArray());

    final EVSpan result = this.outputTopic.readKeyValue().value;

    // Check IDs
    String sid = BaseEncoding.base16().encode(testSpan.getSpanId().toByteArray(), 0, 8);
    String tid = BaseEncoding.base16().encode(testSpan.getTraceId().toByteArray(), 0, 16);
    assertEquals(sid, result.getSpanId());
    assertEquals(tid, result.getTraceId());

  }

  @Test
  void testTimestampTranslation() {
    final Span testSpan = sampleSpan();
    final DumpSpans singleSpanDump = DumpSpans.newBuilder().addSpans(testSpan).build();
    this.inputTopic.pipeInput(testSpan.getSpanId().toByteArray(), singleSpanDump.toByteArray());

    final EVSpan result = this.outputTopic.readKeyValue().value;


    final Instant expectedStartTime = Instant
        .ofEpochSecond(sampleSpan().getStartTime().getSeconds(),
            sampleSpan().getStartTime().getNanos());
    final long expectedEndTime = Instant.ofEpochSecond(sampleSpan().getEndTime().getSeconds(),
        sampleSpan().getEndTime().getNanos()).toEpochMilli();

    // Start and End time
    assertEquals(expectedStartTime, Instant.ofEpochSecond(result.getStartTime().getSeconds(),
        result.getStartTime().getNanoAdjust()));
    assertEquals(expectedEndTime, (long) result.getEndTime());
  }


}
