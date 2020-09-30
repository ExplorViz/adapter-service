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
import net.explorviz.adapter.translation.SpanDynamicConverter;
import net.explorviz.adapter.translation.SpanStructureConverter;
import net.explorviz.adapter.translation.SpanAttributes;
import net.explorviz.adapter.validation.SpanValidator;
import net.explorviz.adapter.validation.StrictValidator;
import net.explorviz.avro.SpanDynamic;
import net.explorviz.avro.SpanStructure;
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
class DumpSpanConverterTest {

  private TopologyTestDriver driver;

  private TestInputTopic<byte[], byte[]> inputTopic;
  private TestOutputTopic<String, SpanStructure> structureOutputTopic;
  private TestOutputTopic<String, SpanDynamic> dynamicOutputTopic;

  private SpecificAvroSerde<SpanStructure> SpanStructureSerDe;
  private SpecificAvroSerde<SpanDynamic> SpanDynamicSerDe;

  @Inject
  KafkaConfig config;

  @BeforeEach
  void setUp() {

    final SchemaRegistryClient schemaRegistryClient = new MockSchemaRegistryClient();

    final SpanValidator v = new StrictValidator();
    final SpanStructureConverter c = new SpanStructureConverter();
    final StructureTransformer structureTransformer = new StructureTransformer(c);
    final DynamicTransformer dynamicTransformer =
        new DynamicTransformer(new SpanDynamicConverter());

    final Topology topology =
        new DumpSpanConverter(schemaRegistryClient, this.config, structureTransformer,
            dynamicTransformer, v).getTopology();

    final Properties props = new Properties();
    props.put(StreamsConfig.APPLICATION_ID_CONFIG, "test");
    props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "dummy:1234");

    this.driver = new TopologyTestDriver(topology, props);

    this.SpanStructureSerDe = new SpecificAvroSerde<>(schemaRegistryClient);
    this.SpanDynamicSerDe = new SpecificAvroSerde<>(schemaRegistryClient);


    this.SpanStructureSerDe.configure(
        Map.of(AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, "http://dummy"), false);

    this.SpanDynamicSerDe.configure(
        Map.of(AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, "http://dummy"), false);


    this.inputTopic = this.driver.createInputTopic("cluster-dump-spans",
        Serdes.ByteArray().serializer(), Serdes.ByteArray().serializer());
    this.structureOutputTopic = this.driver.createOutputTopic(config.getStructureOutTopic(),
        Serdes.String().deserializer(), this.SpanStructureSerDe.deserializer());
    this.dynamicOutputTopic = this.driver.createOutputTopic(config.getDynamicOutTopic(),
        Serdes.String().deserializer(), this.SpanDynamicSerDe.deserializer());
  }

  @AfterEach
  void tearDown() {
    this.SpanStructureSerDe.close();
    this.SpanDynamicSerDe.close();
    this.driver.close();
  }

  private Span sampleSpan() {

    final Map<String, AttributeValue> attrMap = new HashMap<>();
    attrMap.put(SpanAttributes.LANDSCAPE_TOKEN, AttributeValue.newBuilder()
        .setStringValue(TruncatableString.newBuilder().setValue("token")).build());
    attrMap.put(SpanAttributes.HOST_IP, AttributeValue.newBuilder()
        .setStringValue(TruncatableString.newBuilder().setValue("1.2.3.4")).build());
    attrMap.put(SpanAttributes.HOST_NAME, AttributeValue.newBuilder()
        .setStringValue(TruncatableString.newBuilder().setValue("hostname")).build());
    attrMap.put(SpanAttributes.APPLICATION_LANGUAGE, AttributeValue.newBuilder()
        .setStringValue(TruncatableString.newBuilder().setValue("language")).build());
    attrMap.put(SpanAttributes.APPLICATION_NAME, AttributeValue.newBuilder()
        .setStringValue(TruncatableString.newBuilder().setValue("appname")).build());
    attrMap.put(SpanAttributes.APPLICATION_PID, AttributeValue.newBuilder()
        .setStringValue(TruncatableString.newBuilder().setValue("1234")).build());
    attrMap.put(SpanAttributes.METHOD_FQN, AttributeValue.newBuilder()
        .setStringValue(TruncatableString.newBuilder().setValue("net.example.Bar.foo()")).build());


    return Span.newBuilder()
        .setTraceId(
            ByteString.copyFrom("50c246ad9c9883d1558df9f19b9ae7a6", Charset.defaultCharset()))
        .setSpanId(ByteString.copyFrom("7ef83c66eabd5fbb", Charset.defaultCharset()))
        .setParentSpanId(ByteString.copyFrom("7ef83c66efe42aaa", Charset.defaultCharset()))
        .setStartTime(Timestamp.newBuilder().setSeconds(123).setNanos(456).build())
        .setEndTime(Timestamp.newBuilder().setSeconds(456).setNanos(789).build())
        .setAttributes(Span.Attributes.newBuilder().putAllAttributeMap(attrMap))
        .build();
  }

  @Test
  void testAttributeTranslation() {
    final Span testSpan = this.sampleSpan();
    final DumpSpans singleSpanDump = DumpSpans.newBuilder().addSpans(testSpan).build();
    this.inputTopic.pipeInput(testSpan.getSpanId().toByteArray(), singleSpanDump.toByteArray());

    final SpanStructure result = this.structureOutputTopic.readKeyValue().value;

    final Map<String, AttributeValue> attrs = testSpan.getAttributes().getAttributeMapMap();
    final String expectedToken =
        attrs.get(SpanAttributes.LANDSCAPE_TOKEN).getStringValue().getValue();
    final String expectedHostName =
        attrs.get(SpanAttributes.HOST_NAME).getStringValue().getValue();
    final String expectedHostIP = attrs.get(SpanAttributes.HOST_IP).getStringValue().getValue();
    final String expectedAppName =
        attrs.get(SpanAttributes.APPLICATION_NAME).getStringValue().getValue();
    final String expectedAppLang =
        attrs.get(SpanAttributes.APPLICATION_LANGUAGE).getStringValue().getValue();
    final String expectedAppPID =
        attrs.get(SpanAttributes.APPLICATION_PID).getStringValue().getValue();
    final String expectedOperationName =
        attrs.get(SpanAttributes.METHOD_FQN).getStringValue().getValue();

    assertEquals(expectedToken, result.getLandscapeToken(), "Invalid token");

    assertEquals(expectedHostIP, result.getHostIpAddress(), "Invalid host ip address");
    assertEquals(expectedHostName, result.getHostname(), "Invalid host name");

    assertEquals(expectedAppName, result.getAppName(), "Invalid application name");
    assertEquals(expectedAppPID, result.getAppPid(), "Invalid application pid");
    assertEquals(expectedAppLang, result.getAppLanguage(), "Invalid application language");

    assertEquals(expectedOperationName, result.getFullyQualifiedOperationName(),
        "Invalid operation name");

  }


  @Test
  void testIdTranslation() {

    final Span testSpan = this.sampleSpan();
    final DumpSpans singleSpanDump = DumpSpans.newBuilder().addSpans(testSpan).build();
    this.inputTopic.pipeInput(testSpan.getSpanId().toByteArray(), singleSpanDump.toByteArray());

    final SpanStructure result = this.structureOutputTopic.readKeyValue().value;

    // Check IDs
    final String sid = BaseEncoding.base16().encode(testSpan.getSpanId().toByteArray(), 0, 8);
    assertEquals(sid, result.getSpanId());
  }

  @Test
  void testTimestampTranslation() {
    final Span testSpan = this.sampleSpan();
    final DumpSpans singleSpanDump = DumpSpans.newBuilder().addSpans(testSpan).build();
    this.inputTopic.pipeInput(testSpan.getSpanId().toByteArray(), singleSpanDump.toByteArray());

    final SpanStructure result = this.structureOutputTopic.readKeyValue().value;

    final Instant expectedTimestamp = Instant
        .ofEpochSecond(this.sampleSpan().getStartTime().getSeconds(),
            this.sampleSpan().getStartTime().getNanos());

    // Start and End time
    assertEquals(expectedTimestamp, Instant.ofEpochSecond(result.getTimestamp().getSeconds(),
        result.getTimestamp().getNanoAdjust()));
  }

  @Test
  void testDynamicTranslation() {
    final Span testSpan = this.sampleSpan();
    final DumpSpans singleSpanDump = DumpSpans.newBuilder().addSpans(testSpan).build();
    this.inputTopic.pipeInput(testSpan.getSpanId().toByteArray(), singleSpanDump.toByteArray());

    final SpanDynamic result = this.dynamicOutputTopic.readKeyValue().value;
    System.out.println(result);

  }


}
