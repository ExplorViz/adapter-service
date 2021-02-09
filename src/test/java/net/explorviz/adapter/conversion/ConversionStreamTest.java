package net.explorviz.adapter.conversion;

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
import net.explorviz.adapter.conversion.transformer.DynamicTransformer;
import net.explorviz.adapter.conversion.transformer.StructureTransformer;
import net.explorviz.adapter.injection.KafkaConfig;
import net.explorviz.adapter.service.TokenService;
import net.explorviz.adapter.service.converter.AttributesReader;
import net.explorviz.adapter.service.converter.SpanDynamicConverter;
import net.explorviz.adapter.service.converter.SpanStructureConverter;
import net.explorviz.adapter.service.validation.SpanValidator;
import net.explorviz.adapter.service.validation.StrictValidator;
import net.explorviz.avro.SpanDynamic;
import net.explorviz.avro.SpanStructure;
import org.apache.kafka.common.protocol.types.Field;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.TestInputTopic;
import org.apache.kafka.streams.TestOutputTopic;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.TopologyTestDriver;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;

@QuarkusTest
class ConversionStreamTest {

  private TopologyTestDriver driver;

  private TestInputTopic<byte[], byte[]> inputTopic;
  private TestOutputTopic<String, SpanStructure> structureOutputTopic;
  private TestOutputTopic<String, SpanDynamic> dynamicOutputTopic;

  private SpecificAvroSerde<SpanStructure> spanStructureSerDe;
  private SpecificAvroSerde<SpanDynamic> spanDynamicSerDe;

  @Inject
  private KafkaConfig config;

  @BeforeEach
  void setUp() {

    final SchemaRegistryClient schemaRegistryClient = new MockSchemaRegistryClient();

    final TokenService mockTokenService = Mockito.mock(TokenService.class);
    Mockito.when(mockTokenService.exists(Matchers.anyString())).thenReturn(true);
    final SpanValidator v = new StrictValidator(mockTokenService);

    final SpanStructureConverter c = new SpanStructureConverter();
    final StructureTransformer structureTransformer = new StructureTransformer(c);
    final DynamicTransformer dynamicTransformer =
        new DynamicTransformer(new SpanDynamicConverter());

    final Topology topology =
        new ConversionStream(schemaRegistryClient, this.config, structureTransformer,
            dynamicTransformer, v).getTopology();

    final Properties props = new Properties();
    props.put(StreamsConfig.APPLICATION_ID_CONFIG, "test");
    props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "dummy:1234");

    this.driver = new TopologyTestDriver(topology, props);

    this.spanStructureSerDe = new SpecificAvroSerde<>(schemaRegistryClient);
    this.spanDynamicSerDe = new SpecificAvroSerde<>(schemaRegistryClient);


    this.spanStructureSerDe.configure(
        Map.of(AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, "http://dummy"), false);

    this.spanDynamicSerDe.configure(
        Map.of(AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, "http://dummy"), false);


    this.inputTopic = this.driver.createInputTopic("cluster-dump-spans",
        Serdes.ByteArray().serializer(), Serdes.ByteArray().serializer());
    this.structureOutputTopic = this.driver.createOutputTopic(this.config.getStructureOutTopic(),
        Serdes.String().deserializer(), this.spanStructureSerDe.deserializer());
    this.dynamicOutputTopic = this.driver.createOutputTopic(this.config.getDynamicOutTopic(),
        Serdes.String().deserializer(), this.spanDynamicSerDe.deserializer());
  }

  @AfterEach
  void tearDown() {
    this.spanStructureSerDe.close();
    this.spanDynamicSerDe.close();
    this.driver.close();
  }

  private Span sampleSpan() {

    final Map<String, AttributeValue> attrMap = new HashMap<>();
    attrMap.put(AttributesReader.LANDSCAPE_TOKEN, AttributeValue.newBuilder()
        .setStringValue(TruncatableString.newBuilder().setValue("token")).build());
    attrMap.put(AttributesReader.HOST_IP, AttributeValue.newBuilder()
        .setStringValue(TruncatableString.newBuilder().setValue("1.2.3.4")).build());
    attrMap.put(AttributesReader.HOST_NAME, AttributeValue.newBuilder()
        .setStringValue(TruncatableString.newBuilder().setValue("hostname")).build());
    attrMap.put(AttributesReader.APPLICATION_LANGUAGE, AttributeValue.newBuilder()
        .setStringValue(TruncatableString.newBuilder().setValue("language")).build());
    attrMap.put(AttributesReader.APPLICATION_NAME, AttributeValue.newBuilder()
        .setStringValue(TruncatableString.newBuilder().setValue("appname")).build());
    attrMap.put(AttributesReader.APPLICATION_INSTANCE_ID, AttributeValue.newBuilder()
        .setStringValue(TruncatableString.newBuilder().setValue("1234")).build());
    attrMap.put(AttributesReader.METHOD_FQN, AttributeValue.newBuilder()
        .setStringValue(TruncatableString.newBuilder().setValue("net.example.Bar.foo()")).build());

    // CHECKSTYLE:OFF

    return Span.newBuilder()
        .setTraceId(
            ByteString.copyFrom("50c246ad9c9883d1558df9f19b9ae7a6", Charset.defaultCharset()))
        .setSpanId(ByteString.copyFrom("7ef83c66eabd5fbb", Charset.defaultCharset()))
        .setParentSpanId(ByteString.copyFrom("7ef83c66efe42aaa", Charset.defaultCharset()))
        .setStartTime(Timestamp.newBuilder().setSeconds(123).setNanos(456).build())
        .setEndTime(Timestamp.newBuilder().setSeconds(456).setNanos(789).build())
        .setAttributes(Span.Attributes.newBuilder().putAllAttributeMap(attrMap))
        .build();

    // CHECKSTYLE:ON
  }

  @Test
  void testAttributeTranslation() {
    final Span testSpan = this.sampleSpan();
    final DumpSpans singleSpanDump = DumpSpans.newBuilder().addSpans(testSpan).build();
    this.inputTopic.pipeInput(testSpan.getSpanId().toByteArray(), singleSpanDump.toByteArray());

    final SpanStructure result = this.structureOutputTopic.readKeyValue().value;

    final Map<String, AttributeValue> attrs = testSpan.getAttributes().getAttributeMapMap();
    final String expectedToken =
        attrs.get(AttributesReader.LANDSCAPE_TOKEN).getStringValue().getValue();
    final String expectedHostName =
        attrs.get(AttributesReader.HOST_NAME).getStringValue().getValue();
    final String expectedHostIp = attrs.get(AttributesReader.HOST_IP).getStringValue().getValue();
    final String expectedAppName =
        attrs.get(AttributesReader.APPLICATION_NAME).getStringValue().getValue();
    final String expectedAppLang =
        attrs.get(AttributesReader.APPLICATION_LANGUAGE).getStringValue().getValue();
    final long expectedInstanceId = Long.parseLong(
        attrs.get(AttributesReader.APPLICATION_INSTANCE_ID).getStringValue().getValue());
    final String expectedOperationName =
        attrs.get(AttributesReader.METHOD_FQN).getStringValue().getValue();

    assertEquals(expectedToken, result.getLandscapeToken(), "Invalid token");

    assertEquals(expectedHostIp, result.getHostIpAddress(), "Invalid host ip address");
    assertEquals(expectedHostName, result.getHostname(), "Invalid host name");

    assertEquals(expectedAppName, result.getAppName(), "Invalid application name");
    assertEquals(expectedInstanceId, result.getAppInstanceId(), "Invalid application pid");
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

  }


}
