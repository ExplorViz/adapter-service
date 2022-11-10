package net.explorviz.adapter.conversion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.common.io.BaseEncoding;
import com.google.protobuf.ByteString;
import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import io.opentelemetry.proto.collector.trace.v1.ExportTraceServiceRequest;
import io.opentelemetry.proto.common.v1.AnyValue;
import io.opentelemetry.proto.common.v1.KeyValue;
import io.opentelemetry.proto.trace.v1.ResourceSpans;
import io.opentelemetry.proto.trace.v1.ScopeSpans;
import io.opentelemetry.proto.trace.v1.Span;
import io.quarkus.test.junit.QuarkusTest;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


@QuarkusTest
class TopologyTest {

  private TopologyTestDriver driver;

  private TestInputTopic<byte[], byte[]> inputTopic;
  private TestInputTopic<String, TokenEvent> inputTopicTokenEvents;

  private TestOutputTopic<String, SpanStructure> structureOutputTopic;
  private TestOutputTopic<String, SpanDynamic> dynamicOutputTopic;

  private ReadOnlyKeyValueStore<String, TokenEvent> tokenEventStore;

  @ConfigProperty(name = "explorviz.kafka-streams.topics.in")
  /* default */ String inTopic;

  @ConfigProperty(name = "explorviz.kafka-streams.topics.out.structure")
  /* default */ String structureOutTopic;

  @ConfigProperty(name = "explorviz.kafka-streams.topics.out.dynamic")
  /* default */ String dynamicOutTopic;

  @ConfigProperty(name = "explorviz.kafka-streams.topics.in.tokens")
  /* default */ String tokensInTopic; // NOCS

  @Inject
  Topology topology;

  @Inject
  SpecificAvroSerde<SpanDynamic> spanDynamicSerDe; // NOCS

  @Inject
  SpecificAvroSerde<SpanStructure> spanStructureSerDe; // NOCS

  @Inject
  SpecificAvroSerde<TokenEvent> tokenEventSerDe; // NOCS

  @BeforeEach
  void setUp() {

    final Properties config = new Properties();
    config.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
    config.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, SpecificAvroSerde.class.getName());
    config.put(AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, "http://registry:1234");

    this.driver = new TopologyTestDriver(this.topology, config);

    this.inputTopic = this.driver.createInputTopic(this.inTopic, Serdes.ByteArray().serializer(),
        Serdes.ByteArray().serializer());
    this.inputTopicTokenEvents = this.driver.createInputTopic(this.tokensInTopic,
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

    List<KeyValue> attributes = new ArrayList<>();

    attributes.add(KeyValue.newBuilder().setKey(AttributesReader.LANDSCAPE_TOKEN)
        .setValue(AnyValue.newBuilder().setStringValue("token").build()).build());

    attributes.add(KeyValue.newBuilder().setKey(AttributesReader.TOKEN_SECRET)
        .setValue(AnyValue.newBuilder().setStringValue("secret").build()).build());

    attributes.add(KeyValue.newBuilder().setKey(AttributesReader.HOST_NAME)
        .setValue(AnyValue.newBuilder().setStringValue("hostname").build()).build());

    attributes.add(KeyValue.newBuilder().setKey(AttributesReader.HOST_IP)
        .setValue(AnyValue.newBuilder().setStringValue("1.2.3.4").build()).build());

    attributes.add(KeyValue.newBuilder().setKey(AttributesReader.APPLICATION_NAME)
        .setValue(AnyValue.newBuilder().setStringValue("appname").build()).build());

    attributes.add(KeyValue.newBuilder().setKey(AttributesReader.APPLICATION_INSTANCE_ID)
        .setValue(AnyValue.newBuilder().setStringValue("1234").build()).build());

    attributes.add(KeyValue.newBuilder().setKey(AttributesReader.APPLICATION_LANGUAGE)
        .setValue(AnyValue.newBuilder().setStringValue("language").build()).build());

    attributes.add(KeyValue.newBuilder().setKey(AttributesReader.METHOD_FQN)
        .setValue(AnyValue.newBuilder().setStringValue("net.example.Bar.foo()").build()).build());

    // CHECKSTYLE:OFF

    return Span.newBuilder()
        .setTraceId(
            ByteString.copyFrom("50c246ad9c9883d1558df9f19b9ae7a6", Charset.defaultCharset()))
        .setSpanId(ByteString.copyFrom("7ef83c66eabd5fbb", Charset.defaultCharset()))
        .setParentSpanId(ByteString.copyFrom("7ef83c66efe42aaa", Charset.defaultCharset()))
        .setStartTimeUnixNano(1668069002431000000L)
        .setEndTimeUnixNano(1668072086000000000L)
        .addAllAttributes(attributes).build();

    // CHECKSTYLE:ON
  }

  private ExportTraceServiceRequest generateContainerForSpan(Span span) {

    ScopeSpans container2 = ScopeSpans.newBuilder().addSpans(span).build();
    ResourceSpans container1 = ResourceSpans.newBuilder().addScopeSpans(container2).build();
    return ExportTraceServiceRequest.newBuilder()
        .addResourceSpans(container1).build();
  }

  @Test
  void testAttributeTranslation() {
    final Span testSpan = this.sampleSpan();
    final ExportTraceServiceRequest containeredSpan = generateContainerForSpan(testSpan);

    this.inputTopic.pipeInput(testSpan.getSpanId().toByteArray(), containeredSpan.toByteArray());

    final SpanStructure result = this.structureOutputTopic.readKeyValue().value;

    final Map<String, String> attrs = new HashMap<>();

    testSpan.getAttributesList().forEach(keyValue -> {
      attrs.put(keyValue.getKey(), keyValue.getValue().getStringValue());
    });

    final String expectedToken =
        attrs.get(AttributesReader.LANDSCAPE_TOKEN);
    final String expectedHostName =
        attrs.get(AttributesReader.HOST_NAME);
    final String expectedHostIp = attrs.get(AttributesReader.HOST_IP);
    final String expectedAppName =
        attrs.get(AttributesReader.APPLICATION_NAME);
    final String expectedAppLang =
        attrs.get(AttributesReader.APPLICATION_LANGUAGE);
    final String expectedInstanceId =
        attrs.get(AttributesReader.APPLICATION_INSTANCE_ID);
    final String expectedOperationName =
        attrs.get(AttributesReader.METHOD_FQN);

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
    final ExportTraceServiceRequest containeredSpan = generateContainerForSpan(testSpan);

    this.inputTopic.pipeInput(testSpan.getSpanId().toByteArray(), containeredSpan.toByteArray());

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
    final ExportTraceServiceRequest containeredSpan = generateContainerForSpan(testSpan);

    this.inputTopic.pipeInput(testSpan.getSpanId().toByteArray(), containeredSpan.toByteArray());

    final SpanStructure result = this.structureOutputTopic.readKeyValue().value;

    final long expectedTimestamp = this.sampleSpan().getStartTimeUnixNano();

    assertEquals(expectedTimestamp,
        result.getTimestampInEpochMilli());
  }

  @Test
  void testDynamicTranslation() {
    final Span testSpan = this.sampleSpan();
    final ExportTraceServiceRequest containeredSpan = generateContainerForSpan(testSpan);

    this.inputTopic.pipeInput(testSpan.getSpanId().toByteArray(), containeredSpan.toByteArray());

    final SpanDynamic result = this.dynamicOutputTopic.readValue();

    final Map<String, String> attrs = new HashMap<>();

    testSpan.getAttributesList().forEach(keyValue -> {
      attrs.put(keyValue.getKey(), keyValue.getValue().getStringValue());
    });

    final String expectedToken =
        attrs.get(AttributesReader.LANDSCAPE_TOKEN);

    final String expectedSpanId = IdHelper.converterSpanId(testSpan.getSpanId().toByteArray());

    final String expectedParentSpanId =
        IdHelper.converterSpanId(testSpan.getParentSpanId().toByteArray());

    final String expectedHashValue = HashHelper.fromSpanAttributes(new AttributesReader(testSpan));

    final long expectedStartTimeInMillisec = testSpan.getStartTimeUnixNano() / 1000000L;

    final long expectedEndTimeInMillisec = testSpan.getEndTimeUnixNano() / 1000000L;

    assertEquals(expectedToken, result.getLandscapeToken(), "Invalid token");
    assertEquals(expectedSpanId, result.getSpanId(), "Invalid span id");
    assertEquals(expectedParentSpanId, result.getParentSpanId(), "Invalid parent span id");
    assertEquals(expectedHashValue, result.getHashCode(), "Invalid hash code");
    assertEquals(expectedStartTimeInMillisec, result.getStartTimeEpochMilli(),
        "Invalid start time");
    assertEquals(expectedEndTimeInMillisec, result.getEndTimeEpochMilli(), "Invalid end time");
  }

  @Test
  void testTokenEventCreateInteractiveStateStoreQuery() {

    final Span testSpan = this.sampleSpan();

    final Map<String, String> attrs = new HashMap<>();

    testSpan.getAttributesList().forEach(keyValue -> {
      attrs.put(keyValue.getKey(), keyValue.getValue().getStringValue());
    });

    final String expectedTokenValue =
        attrs.get(AttributesReader.LANDSCAPE_TOKEN);
    final String expectedSecret =
        attrs.get(AttributesReader.TOKEN_SECRET);

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

  @Test
  void testTokenEventDeleteInteractiveStateStoreQuery() {

    final Span testSpan = this.sampleSpan();

    final Map<String, String> attrs = new HashMap<>();

    testSpan.getAttributesList().forEach(keyValue -> {
      attrs.put(keyValue.getKey(), keyValue.getValue().getStringValue());
    });

    final String expectedTokenValue =
        attrs.get(AttributesReader.LANDSCAPE_TOKEN);
    final String expectedSecret =
        attrs.get(AttributesReader.TOKEN_SECRET);

    final LandscapeToken expectedToken = LandscapeToken.newBuilder().setSecret(expectedSecret)
        .setValue(expectedTokenValue).setOwnerId("testOwner").setCreated(123L).setAlias("").build();

    final TokenEvent expectedTokenEvent = TokenEvent.newBuilder().setType(EventType.CREATED)
        .setToken(expectedToken).setClonedToken("").build();

    this.inputTopicTokenEvents.pipeInput(expectedTokenValue, expectedTokenEvent);

    // use mocked state store
    final TokenService tokenServie = new TokenService(this.tokenEventStore);

    final boolean resultFromStateStore = tokenServie.validLandscapeTokenValue(expectedTokenValue);
    assertTrue(resultFromStateStore, "Invalid token event in state store");

    // Now delete event

    this.inputTopicTokenEvents.pipeInput(expectedTokenValue, null);

    // Use state store of TopologyTestDriver instead of real in-memory one, since it is not
    // available for tests
    final boolean resultFromStateStore2 = tokenServie.validLandscapeTokenValue(expectedTokenValue);

    assertFalse(resultFromStateStore2, "Invalid token event in state store, should be null");
  }

  @Test
  void testTokenEventInteractiveStateStoreQuery() {

    final Span testSpan = this.sampleSpan();

    final Map<String, String> attrs = new HashMap<>();

    testSpan.getAttributesList().forEach(keyValue -> {
      attrs.put(keyValue.getKey(), keyValue.getValue().getStringValue());
    });

    final String expectedTokenValue =
        attrs.get(AttributesReader.LANDSCAPE_TOKEN);
    final String expectedSecret =
        attrs.get(AttributesReader.TOKEN_SECRET);

    final LandscapeToken expectedToken = LandscapeToken.newBuilder().setSecret(expectedSecret)
        .setValue(expectedTokenValue).setOwnerId("testOwner").setCreated(123L).setAlias("").build();

    final TokenEvent expectedTokenEvent = TokenEvent.newBuilder().setType(EventType.CREATED)
        .setToken(expectedToken).setClonedToken("").build();

    this.inputTopicTokenEvents.pipeInput(expectedTokenValue, expectedTokenEvent);

    // use mocked state store
    final TokenService tokenServie = new TokenService(this.tokenEventStore);

    final boolean resultFromStateStore =
        tokenServie.validLandscapeTokenValueAndSecret(expectedTokenValue, expectedSecret);
    assertTrue(resultFromStateStore, "Invalid token event in state store");

    // Now delete event

    this.inputTopicTokenEvents.pipeInput(expectedTokenValue, null);

    // Use state store of TopologyTestDriver instead of real in-memory one, since it is not
    // available for tests
    final boolean resultFromStateStore2 =
        tokenServie.validLandscapeTokenValueAndSecret(expectedTokenValue, expectedSecret);

    assertFalse(resultFromStateStore2, "Invalid token event in state store, should be null");
  }

  @Test
  void testFilteringTokenEventInteractiveStateStoreQuery() {

    final Span testSpan = this.sampleSpan();

    final Map<String, String> attrs = new HashMap<>();

    testSpan.getAttributesList().forEach(keyValue -> {
      attrs.put(keyValue.getKey(), keyValue.getValue().getStringValue());
    });

    final String expectedTokenValue =
        attrs.get(AttributesReader.LANDSCAPE_TOKEN);
    final String expectedSecret =
        attrs.get(AttributesReader.TOKEN_SECRET);

    final LandscapeToken expectedToken = LandscapeToken.newBuilder().setSecret(expectedSecret)
        .setValue(expectedTokenValue).setOwnerId("testOwner").setCreated(123L).setAlias("").build();

    for (final EventType eventType : EventType.values()) {
      if (!eventType.equals(EventType.CREATED)) {
        final TokenEvent expectedTokenEvent = TokenEvent.newBuilder().setType(eventType)
            .setToken(expectedToken).setClonedToken("").build();

        this.inputTopicTokenEvents.pipeInput(expectedTokenValue, expectedTokenEvent);
      }
    }

    assertTrue(this.tokenEventStore.approximateNumEntries() == 0,
        "State store not empty, but should be empty");

    // use mocked state store
    final TokenService tokenServie = new TokenService(this.tokenEventStore);

    final boolean resultFromStateStore =
        tokenServie.validLandscapeTokenValueAndSecret(expectedTokenValue, expectedSecret);
    assertFalse(resultFromStateStore, "Invalid token event in state store");
  }


}
