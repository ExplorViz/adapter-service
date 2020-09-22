package net.explorviz.adapter.conversion.opentelemetry.stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.protobuf.ByteString;
import io.confluent.kafka.schemaregistry.client.MockSchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient;
import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import io.opentelemetry.proto.common.v1.AnyValue;
import io.opentelemetry.proto.common.v1.KeyValue;
import io.opentelemetry.proto.trace.v1.Span;
import io.quarkus.test.junit.QuarkusTest;
import java.nio.charset.Charset;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import javax.inject.Inject;

import net.explorviz.adapter.conversion.IdHelper;
import net.explorviz.adapter.conversion.opentelemetry.converter.OtSpanAttributes;
import net.explorviz.adapter.conversion.opentelemetry.converter.OtSpanDynamicConverter;
import net.explorviz.adapter.conversion.opentelemetry.converter.OtSpanStructureConverter;
import net.explorviz.adapter.injection.KafkaConfig;
import net.explorviz.adapter.validation.NoOpStructureSanitizer;
import net.explorviz.adapter.validation.SpanStructureSanitizer;
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
class SpanConverterStreamTest {

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
    final SpanStructureSanitizer s = new NoOpStructureSanitizer();
    final OtSpanStructureConverter c = new OtSpanStructureConverter();
    final StructureTransformer structureTransformer = new StructureTransformer(s, c);
    final DynamicTransformer dynamicTransformer =
        new DynamicTransformer(new OtSpanDynamicConverter());

    final Topology topology =
        new SpanConverterStream(schemaRegistryClient, this.config, structureTransformer,
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


    this.inputTopic = this.driver.createInputTopic(config.getInTopicOpenTelemetry(),
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

    final List<KeyValue> attrMap = new ArrayList<>();
    attrMap.add(KeyValue.newBuilder().setKey(OtSpanAttributes.LANDSCAPE_TOKEN)
        .setValue(AnyValue.newBuilder().setStringValue("token").build()).build());
    attrMap.add(KeyValue.newBuilder().setKey(OtSpanAttributes.HOST_IP)
        .setValue(AnyValue.newBuilder().setStringValue("1.2.3.4").build()).build());
    attrMap.add(KeyValue.newBuilder().setKey(OtSpanAttributes.HOST_NAME)
        .setValue(AnyValue.newBuilder().setStringValue("hostname").build()).build());
    attrMap.add(KeyValue.newBuilder().setKey(OtSpanAttributes.APPLICATION_LANGUAGE)
        .setValue(AnyValue.newBuilder().setStringValue("language").build()).build());
    attrMap.add(KeyValue.newBuilder().setKey(OtSpanAttributes.APPLICATION_NAME)
        .setValue(AnyValue.newBuilder().setStringValue("appname").build()).build());
    attrMap.add(KeyValue.newBuilder().setKey(OtSpanAttributes.APPLICATION_PID)
        .setValue(AnyValue.newBuilder().setStringValue("1234").build()).build());
    attrMap.add(KeyValue.newBuilder().setKey(OtSpanAttributes.METHOD_FQN)
        .setValue(AnyValue.newBuilder().setStringValue("net.example.Bar.foo()").build()).build());


    Span.Builder builder = Span.newBuilder()
        .setTraceId(
            ByteString.copyFrom("50c246ad9c9883d1558df9f19b9ae7a6", Charset.defaultCharset()))
        .setSpanId(ByteString.copyFrom("7ef83c66eabd5fbb", Charset.defaultCharset()))
        .setParentSpanId(ByteString.copyFrom("7ef83c66efe42aaa", Charset.defaultCharset()))
        .setStartTimeUnixNano(1600417977219429929L)
        .setEndTimeUnixNano(1600417977219529929L)
        .addAllAttributes(attrMap);

    return builder.build();
  }

  @Test
  void testAttributeTranslation() {
    final Span testSpan = this.sampleSpan();
    this.inputTopic.pipeInput(testSpan.getSpanId().toByteArray(), testSpan.toByteArray());

    assert (!structureOutputTopic.isEmpty());

    final SpanStructure result = this.structureOutputTopic.readKeyValue().value;

    OtSpanAttributes expected = new OtSpanAttributes(testSpan);

    assertEquals(expected.getLandscapeToken(), result.getLandscapeToken(), "Invalid token");

    assertEquals(expected.getHostIPAddress(), result.getHostIpAddress(), "Invalid host ip address");
    assertEquals(expected.getHostName(), result.getHostname(), "Invalid host name");

    assertEquals(expected.getApplicationName(), result.getAppName(), "Invalid application name");
    assertEquals(expected.getApplicationPID(), result.getAppPid(), "Invalid application pid");
    assertEquals(expected.getApplicationLanguage(), result.getAppLanguage(),
        "Invalid application language");

    assertEquals(expected.getMethodFQN(), result.getFullyQualifiedOperationName(),
        "Invalid operation name");

  }


  @Test
  void testIdTranslation() {

    final Span testSpan = this.sampleSpan();
    this.inputTopic.pipeInput(testSpan.getSpanId().toByteArray(), testSpan.toByteArray());

    final SpanStructure result = this.structureOutputTopic.readKeyValue().value;

    // Check IDs
    final String sid = IdHelper.converterSpanId(testSpan.getSpanId().toByteArray());
    assertEquals(sid, result.getSpanId());

  }

  @Test
  void testTimestampTranslation() {
    final Span testSpan = this.sampleSpan();
    this.inputTopic.pipeInput(testSpan.getSpanId().toByteArray(), testSpan.toByteArray());

    final SpanStructure result = this.structureOutputTopic.readKeyValue().value;

    final Instant expectedTimestamp =
        Instant.EPOCH.plus(testSpan.getStartTimeUnixNano(), ChronoUnit.NANOS);

    // Start and End time
    assertEquals(expectedTimestamp, Instant.ofEpochSecond(result.getTimestamp().getSeconds(),
        result.getTimestamp().getNanoAdjust()));
  }

  @Test
  void testDynamicTranslation() {
    final Span testSpan = this.sampleSpan();
    this.inputTopic.pipeInput(testSpan.getSpanId().toByteArray(), testSpan.toByteArray());

    final SpanDynamic result = this.dynamicOutputTopic.readKeyValue().value;
    System.out.println(result);

  }

  // TODO: Add tests for dynamic data


}
