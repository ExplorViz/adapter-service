package net.explorviz.adapter.conversion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import com.google.common.io.BaseEncoding;
import com.google.protobuf.ByteString;
import com.google.protobuf.Timestamp;
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
import net.explorviz.adapter.service.TokenService;
import net.explorviz.adapter.service.converter.AttributesReader;
import net.explorviz.adapter.service.converter.HashHelper;
import net.explorviz.adapter.service.converter.IdHelper;
import net.explorviz.avro.EventType;
import net.explorviz.avro.LandscapeToken;
import net.explorviz.avro.SpanDynamic;
import net.explorviz.avro.SpanStructure;
import net.explorviz.avro.TokenEvent;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.TestInputTopic;
import org.apache.kafka.streams.TestOutputTopic;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.TopologyTestDriver;
import org.apache.kafka.streams.state.KeyValueStore;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@QuarkusTest
class TopologyTest {

  private static final Logger LOGGER = LoggerFactory.getLogger(TopologyTest.class);

  private TopologyTestDriver driver;

  private TestInputTopic<byte[], byte[]> inputTopic;
  private TestInputTopic<String, TokenEvent> inputTopicTokenEvents;

  private TestOutputTopic<String, SpanStructure> structureOutputTopic;
  private TestOutputTopic<String, SpanDynamic> dynamicOutputTopic;

  private KeyValueStore<String, TokenEvent> tokenEventStore;

  @ConfigProperty(name = "explorviz.kafka-streams.topics.in")
  /* default */ String inTopic;

  @ConfigProperty(name = "explorviz.kafka-streams.topics.out.structure")
  /* default */ String structureOutTopic;

  @ConfigProperty(name = "explorviz.kafka-streams.topics.out.dynamic")
  /* default */ String dynamicOutTopic;

  @Inject
  Topology topology;

  @Inject
  SpecificAvroSerde<SpanDynamic> spanDynamicSerDe; // NOCS

  @Inject
  SpecificAvroSerde<SpanStructure> spanStructureSerDe; // NOCS

  @Inject
  SpecificAvroSerde<TokenEvent> tokenEventSerDe; // NOCS

  @Inject
  TokenService tokenService; // NOCS

  @BeforeEach
  void setUp() {

    final Properties config = new Properties();
    config.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
    config.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, SpecificAvroSerde.class.getName());
    config.put(AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, "http://registry:1234");

    this.driver = new TopologyTestDriver(this.topology, config);

    this.inputTopic = this.driver.createInputTopic(this.inTopic, Serdes.ByteArray().serializer(),
        Serdes.ByteArray().serializer());
    this.inputTopicTokenEvents = this.driver.createInputTopic("token-events",
        Serdes.String().serializer(), this.tokenEventSerDe.serializer());
    this.structureOutputTopic = this.driver.createOutputTopic(this.structureOutTopic,
        Serdes.String().deserializer(), this.spanStructureSerDe.deserializer());
    this.dynamicOutputTopic = this.driver.createOutputTopic(this.dynamicOutTopic,
        Serdes.String().deserializer(), this.spanDynamicSerDe.deserializer());

    this.tokenEventStore = this.driver.getKeyValueStore("token-events-global-store");

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
    attrMap.put(AttributesReader.TOKEN_SECRET, AttributeValue.newBuilder()
        .setStringValue(TruncatableString.newBuilder().setValue("secret")).build());
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
        .setAttributes(Span.Attributes.newBuilder().putAllAttributeMap(attrMap)).build();

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
    final String expectedInstanceId =
        attrs.get(AttributesReader.APPLICATION_INSTANCE_ID).getStringValue().getValue();
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

    assertFalse(this.structureOutputTopic.isEmpty(),
        "output topic is empty, but should contain a data record");

    final SpanStructure result = this.structureOutputTopic.readValue();

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

    final Instant expectedTimestamp = Instant.ofEpochSecond(
        this.sampleSpan().getStartTime().getSeconds(), this.sampleSpan().getStartTime().getNanos());

    // Start and End time
    assertEquals(expectedTimestamp, Instant.ofEpochSecond(result.getTimestamp().getSeconds(),
        result.getTimestamp().getNanoAdjust()));
  }

  @Test
  void testDynamicTranslation() {
    final Span testSpan = this.sampleSpan();
    final DumpSpans singleSpanDump = DumpSpans.newBuilder().addSpans(testSpan).build();
    this.inputTopic.pipeInput(testSpan.getSpanId().toByteArray(), singleSpanDump.toByteArray());

    final SpanDynamic result = this.dynamicOutputTopic.readValue();

    final Map<String, AttributeValue> attrs = testSpan.getAttributes().getAttributeMapMap();
    final String expectedToken =
        attrs.get(AttributesReader.LANDSCAPE_TOKEN).getStringValue().getValue();

    final String expectedSpanId = IdHelper.converterSpanId(testSpan.getSpanId().toByteArray());

    final String expectedParentSpanId =
        IdHelper.converterSpanId(testSpan.getParentSpanId().toByteArray());

    final String expectedHashValue = HashHelper.fromSpanAttributes(new AttributesReader(testSpan));

    final net.explorviz.avro.Timestamp expectedStartTime = new net.explorviz.avro.Timestamp(
        testSpan.getStartTime().getSeconds(), testSpan.getStartTime().getNanos());

    final net.explorviz.avro.Timestamp exectedEndTime = new net.explorviz.avro.Timestamp(
        testSpan.getEndTime().getSeconds(), testSpan.getEndTime().getNanos());


    assertEquals(expectedToken, result.getLandscapeToken(), "Invalid token");
    assertEquals(expectedSpanId, result.getSpanId(), "Invalid span id");
    assertEquals(expectedParentSpanId, result.getParentSpanId(), "Invalid parent span id");
    assertEquals(expectedHashValue, result.getHashCode(), "Invalid hash code");
    assertEquals(expectedStartTime, result.getStartTime(), "Invalid start time");
    assertEquals(exectedEndTime, result.getEndTime(), "Invalid end time");
  }

  @Test
  void testTokenEventInteractiveStateStoreQuery() {

    final Span testSpan = this.sampleSpan();
    final Map<String, AttributeValue> attrs = testSpan.getAttributes().getAttributeMapMap();
    final String expectedTokenValue =
        attrs.get(AttributesReader.LANDSCAPE_TOKEN).getStringValue().getValue();
    final String expectedSecret =
        attrs.get(AttributesReader.TOKEN_SECRET).getStringValue().getValue();

    final LandscapeToken expectedToken = LandscapeToken.newBuilder().setSecret(expectedSecret)
        .setValue(expectedTokenValue).setOwnerId("testOwner").setCreated(123L).setAlias("").build();

    final TokenEvent expectedTokenEvent = TokenEvent.newBuilder().setType(EventType.CREATED)
        .setToken(expectedToken).setClonedToken("").build();

    this.inputTopicTokenEvents.pipeInput(expectedTokenValue, expectedTokenEvent);

    // Use state store of TopologyTestDriver instead of real in-memory one, since it is not
    // available for tests
    final TokenEvent resultFromStateStore = this.tokenEventStore.get(expectedTokenValue);

    assertEquals(resultFromStateStore, expectedTokenEvent, "Invalid token event in state store");
  }


}
