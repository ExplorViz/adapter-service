package net.explorviz.adapter.conversion;

import com.google.common.io.BaseEncoding
import com.google.protobuf.ByteString
import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde
import io.opentelemetry.proto.collector.trace.v1.ExportTraceServiceRequest
import io.opentelemetry.proto.common.v1.AnyValue
import io.opentelemetry.proto.common.v1.KeyValue
import io.opentelemetry.proto.trace.v1.ResourceSpans
import io.opentelemetry.proto.trace.v1.ScopeSpans
import io.opentelemetry.proto.trace.v1.Span
import io.quarkus.test.junit.QuarkusTest
import jakarta.inject.Inject
import net.explorviz.adapter.service.TokenService
import net.explorviz.adapter.service.converter.AttributesReader
import net.explorviz.adapter.service.converter.IdHelper
import net.explorviz.avro.EventType
import net.explorviz.avro.LandscapeToken
import net.explorviz.avro.TokenEvent
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.*
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore
import org.eclipse.microprofile.config.inject.ConfigProperty
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.nio.charset.Charset
import java.util.*


@QuarkusTest
class TopologyTest {

  @ConfigProperty(name = "explorviz.kafka-streams.topics.in")
  lateinit var inTopic: String

  @ConfigProperty(name = "explorviz.kafka-streams.topics.out.spans")
  lateinit var spanOutTopicKey: String

  @ConfigProperty(name = "explorviz.kafka-streams.topics.in.tokens")
  lateinit var tokensInTopic: String

  @Inject
  lateinit var topology: Topology

  @Inject
  lateinit var spanSerDe: SpecificAvroSerde<net.explorviz.avro.Span>

  @Inject
  lateinit var tokenEventSerDe: SpecificAvroSerde<TokenEvent>

  private var driver: TopologyTestDriver? = null
  private var inputTopic: TestInputTopic<ByteArray, ByteArray>? = null
  private var inputTopicTokenEvents: TestInputTopic<String, TokenEvent>? = null
  private var spanOutputTopic: TestOutputTopic<String, net.explorviz.avro.Span>? = null
  private var tokenEventStore: ReadOnlyKeyValueStore<String, TokenEvent>? = null

  @BeforeEach
  fun setUp() {
    val config = Properties().apply {
      put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String()::class.java.name)
      put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, SpecificAvroSerde::class.java.name)
      put(AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, "http://registry:1234")
    }

    driver = TopologyTestDriver(topology, config)

    inputTopic = driver!!.createInputTopic(
      inTopic, Serdes.ByteArray().serializer(), Serdes.ByteArray().serializer()
    )
    inputTopicTokenEvents = driver!!.createInputTopic(
      tokensInTopic, Serdes.String().serializer(), tokenEventSerDe.serializer()
    )
    spanOutputTopic = driver!!.createOutputTopic(
      spanOutTopicKey, Serdes.String().deserializer(), spanSerDe.deserializer()
    )

    tokenEventStore = driver!!.getKeyValueStore("token-events-global-store")
  }


  @AfterEach
  fun tearDown() {
    spanSerDe.close()
    driver?.close()
  }

  private fun sampleSpan(): Span {
    val attributes = listOf(
      KeyValue.newBuilder().setKey(AttributesReader.LANDSCAPE_TOKEN)
        .setValue(AnyValue.newBuilder().setStringValue("token").build()).build(),
      KeyValue.newBuilder().setKey(AttributesReader.GIT_COMMIT_CHECKSUM)
        .setValue(AnyValue.newBuilder().setStringValue("testGitCommit").build()).build(),
      KeyValue.newBuilder().setKey(AttributesReader.TOKEN_SECRET)
        .setValue(AnyValue.newBuilder().setStringValue("secret").build()).build(),
      KeyValue.newBuilder().setKey(AttributesReader.HOST_NAME)
        .setValue(AnyValue.newBuilder().setStringValue("hostname").build()).build(),
      KeyValue.newBuilder().setKey(AttributesReader.HOST_IP)
        .setValue(AnyValue.newBuilder().setStringValue("1.2.3.4").build()).build(),
      KeyValue.newBuilder().setKey(AttributesReader.APPLICATION_NAME)
        .setValue(AnyValue.newBuilder().setStringValue("appname").build()).build(),
      KeyValue.newBuilder().setKey(AttributesReader.APPLICATION_INSTANCE_ID)
        .setValue(AnyValue.newBuilder().setStringValue("1234").build()).build(),
      KeyValue.newBuilder().setKey(AttributesReader.APPLICATION_LANGUAGE)
        .setValue(AnyValue.newBuilder().setStringValue("language").build()).build(),
      KeyValue.newBuilder().setKey(AttributesReader.METHOD_FQN)
        .setValue(AnyValue.newBuilder().setStringValue("net.example.Bar.foo()").build()).build()
    )

    return Span.newBuilder()
      .setTraceId(ByteString.copyFrom("50c246ad9c9883d1558df9f19b9ae7a6", Charset.defaultCharset()))
      .setSpanId(ByteString.copyFrom("7ef83c66eabd5fbb", Charset.defaultCharset()))
      .setParentSpanId(ByteString.copyFrom("7ef83c66efe42aaa", Charset.defaultCharset()))
      .setStartTimeUnixNano(1668069002431000000L)
      .setEndTimeUnixNano(1668072086000000000L)
      .addAllAttributes(attributes)
      .build()
  }

  private fun generateContainerForSpan(span: Span): ExportTraceServiceRequest {
    val container2 = ScopeSpans.newBuilder().addSpans(span).build()
    val container1 = ResourceSpans.newBuilder().addScopeSpans(container2).build()
    return ExportTraceServiceRequest.newBuilder()
      .addResourceSpans(container1).build()
  }

  @Test
  fun testAttributeTranslation() {
    val testSpan = sampleSpan()
    val containeredSpan = generateContainerForSpan(testSpan)

    inputTopic!!.pipeInput(testSpan.spanId.toByteArray(), containeredSpan.toByteArray())
    val result = spanOutputTopic!!.readKeyValue().value

    val attrs = testSpan.attributesList.associate { it.key to it.value.stringValue }

    assertEquals(attrs[AttributesReader.LANDSCAPE_TOKEN], result.landscapeToken, "Invalid token")
    assertEquals(
      attrs[AttributesReader.GIT_COMMIT_CHECKSUM],
      result.gitCommitChecksum,
      "Invalid Git Commit Checksum"
    )
    assertEquals(attrs[AttributesReader.HOST_IP], result.hostIpAddress, "Invalid host ip address")
    assertEquals(attrs[AttributesReader.HOST_NAME], result.hostname, "Invalid host name")
    assertEquals(
      attrs[AttributesReader.APPLICATION_NAME],
      result.appName,
      "Invalid application name"
    )
    assertEquals(
      attrs[AttributesReader.APPLICATION_INSTANCE_ID],
      result.appInstanceId,
      "Invalid application pid"
    )
    assertEquals(
      attrs[AttributesReader.APPLICATION_LANGUAGE],
      result.appLanguage,
      "Invalid application language"
    )
    assertEquals(
      attrs[AttributesReader.METHOD_FQN],
      result.fullyQualifiedOperationName,
      "Invalid operation name"
    )
  }

  @Test
  fun testIdTranslation() {
    val testSpan = sampleSpan()
    val containeredSpan = generateContainerForSpan(testSpan)

    inputTopic!!.pipeInput(testSpan.spanId.toByteArray(), containeredSpan.toByteArray())

    assertFalse(
      spanOutputTopic!!.isEmpty,
      "Output topic is empty, but should contain a data record"
    )

    val result = spanOutputTopic!!.readValue()

    val sid = BaseEncoding.base16().encode(testSpan.spanId.toByteArray(), 0, 8)
    assertEquals(sid, result.spanId)
  }

  @Test
  fun testTimestampTranslation() {
    val testSpan = sampleSpan()
    val containeredSpan = generateContainerForSpan(testSpan)

    inputTopic!!.pipeInput(testSpan.spanId.toByteArray(), containeredSpan.toByteArray())
    val result = spanOutputTopic!!.readKeyValue().value

    val expectedTimestamp = sampleSpan().startTimeUnixNano / 1_000_000L
    assertEquals(expectedTimestamp, result.startTimeEpochMilli)
  }

  @Test
  fun testDynamicTranslation() {
    val testSpan = sampleSpan()
    val containeredSpan = generateContainerForSpan(testSpan)

    inputTopic!!.pipeInput(testSpan.spanId.toByteArray(), containeredSpan.toByteArray())
    val result = spanOutputTopic!!.readValue()

    val attrs = testSpan.attributesList.associate { it.key to it.value.stringValue }

    val expectedToken = attrs[AttributesReader.LANDSCAPE_TOKEN]
    val expectedSpanId = IdHelper.convertSpanId(testSpan.spanId.toByteArray())
    val expectedParentSpanId = IdHelper.convertSpanId(testSpan.parentSpanId.toByteArray())
    val expectedStartTimeInMillisec = testSpan.startTimeUnixNano / 1_000_000L
    val expectedEndTimeInMillisec = testSpan.endTimeUnixNano / 1_000_000L

    assertEquals(expectedToken, result.landscapeToken, "Invalid token")
    assertEquals(expectedSpanId, result.spanId, "Invalid span ID")
    assertEquals(expectedParentSpanId, result.parentSpanId, "Invalid parent span ID")
    assertEquals(expectedStartTimeInMillisec, result.startTimeEpochMilli, "Invalid start time")
    assertEquals(expectedEndTimeInMillisec, result.endTimeEpochMilli, "Invalid end time")
  }

  @Test
  fun testTokenEventCreateInteractiveStateStoreQuery() {
    val testSpan = sampleSpan()

    val attrs = testSpan.attributesList.associate { it.key to it.value.stringValue }

    val expectedTokenValue = attrs[AttributesReader.LANDSCAPE_TOKEN]!!
    val expectedSecret = attrs[AttributesReader.TOKEN_SECRET]!!

    val expectedToken = LandscapeToken.newBuilder()
      .setSecret(expectedSecret)
      .setValue(expectedTokenValue)
      .setOwnerId("testOwner")
      .setCreated(123L)
      .setAlias("")
      .build()

    val expectedTokenEvent = TokenEvent.newBuilder()
      .setType(EventType.CREATED)
      .setToken(expectedToken)
      .setClonedToken("")
      .build()

    inputTopicTokenEvents!!.pipeInput(expectedTokenValue, expectedTokenEvent)

    val resultFromStateStore = tokenEventStore!!.get(expectedTokenValue)
    assertEquals(resultFromStateStore, expectedTokenEvent, "Invalid token event in state store")
  }

  @Test
  fun testTokenEventDeleteInteractiveStateStoreQuery() {
    val testSpan = sampleSpan()

    val attrs = testSpan.attributesList.associate { it.key to it.value.stringValue }

    val expectedTokenValue = attrs[AttributesReader.LANDSCAPE_TOKEN]!!
    val expectedSecret = attrs[AttributesReader.TOKEN_SECRET]!!

    val expectedToken = LandscapeToken.newBuilder()
      .setSecret(expectedSecret)
      .setValue(expectedTokenValue)
      .setOwnerId("testOwner")
      .setCreated(123L)
      .setAlias("")
      .build()

    val expectedTokenEvent = TokenEvent.newBuilder()
      .setType(EventType.CREATED)
      .setToken(expectedToken)
      .setClonedToken("")
      .build()

    inputTopicTokenEvents!!.pipeInput(expectedTokenValue, expectedTokenEvent)

    val tokenService = TokenService(tokenEventStore!!)

    val resultFromStateStore = tokenService.validLandscapeTokenValue(expectedTokenValue)
    assertTrue(resultFromStateStore, "Invalid token event in state store")

    // Now delete the event
    inputTopicTokenEvents!!.pipeInput(expectedTokenValue, null)

    val resultFromStateStore2 = tokenService.validLandscapeTokenValue(expectedTokenValue)
    assertFalse(resultFromStateStore2, "Invalid token event in state store, should be null")
  }

  @Test
  fun testTokenEventInteractiveStateStoreQuery() {
    val testSpan = sampleSpan()

    val attrs = testSpan.attributesList.associate { it.key to it.value.stringValue }

    val expectedTokenValue = attrs[AttributesReader.LANDSCAPE_TOKEN]!!
    val expectedSecret = attrs[AttributesReader.TOKEN_SECRET]!!

    val expectedToken = LandscapeToken.newBuilder()
      .setSecret(expectedSecret)
      .setValue(expectedTokenValue)
      .setOwnerId("testOwner")
      .setCreated(123L)
      .setAlias("")
      .build()

    val expectedTokenEvent = TokenEvent.newBuilder()
      .setType(EventType.CREATED)
      .setToken(expectedToken)
      .setClonedToken("")
      .build()

    inputTopicTokenEvents!!.pipeInput(expectedTokenValue, expectedTokenEvent)

    val tokenService = TokenService(tokenEventStore!!)

    val resultFromStateStore =
      tokenService.validLandscapeTokenValueAndSecret(expectedTokenValue, expectedSecret)
    assertTrue(resultFromStateStore, "Invalid token event in state store")

    // Now delete the event
    inputTopicTokenEvents!!.pipeInput(expectedTokenValue, null)

    val resultFromStateStore2 =
      tokenService.validLandscapeTokenValueAndSecret(expectedTokenValue, expectedSecret)
    assertFalse(resultFromStateStore2, "Invalid token event in state store, should be null")
  }

  @Test
  fun testFilteringTokenEventInteractiveStateStoreQuery() {
    val testSpan = sampleSpan()

    val attrs = testSpan.attributesList.associate { it.key to it.value.stringValue }

    val expectedTokenValue = attrs[AttributesReader.LANDSCAPE_TOKEN]!!
    val expectedSecret = attrs[AttributesReader.TOKEN_SECRET]!!

    val expectedToken = LandscapeToken.newBuilder()
      .setSecret(expectedSecret)
      .setValue(expectedTokenValue)
      .setOwnerId("testOwner")
      .setCreated(123L)
      .setAlias("")
      .build()

    EventType.values().forEach { eventType ->
      if (eventType != EventType.CREATED) {
        val expectedTokenEvent = TokenEvent.newBuilder()
          .setType(eventType)
          .setToken(expectedToken)
          .setClonedToken("")
          .build()

        inputTopicTokenEvents!!.pipeInput(expectedTokenValue, expectedTokenEvent)
      }
    }

    assertEquals(
      0,
      tokenEventStore!!.approximateNumEntries(),
      "State store not empty, but should be empty"
    )

    val tokenService = TokenService(tokenEventStore!!)

    val resultFromStateStore =
      tokenService.validLandscapeTokenValueAndSecret(expectedTokenValue, expectedSecret)
    assertFalse(resultFromStateStore, "Invalid token event in state store")
  }

}
