package net.explorviz.kafka;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.rest.exceptions.RestClientException;
import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import io.opencensus.proto.dump.DumpSpans;
import io.opencensus.proto.trace.v1.Span;
import io.quarkus.test.junit.QuarkusTest;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.time.Instant;
import java.util.Map;
import java.util.Properties;
import javax.inject.Inject;
import net.explorviz.avro.EVSpan;
import org.apache.kafka.common.serialization.Deserializer;
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
class SpanTranslatorTest {

  private TopologyTestDriver driver;

  TestInputTopic<byte[], byte[]> inputTopic;
  TestOutputTopic<String, EVSpan> outputTopic;

  @Inject
  SchemaRegistryClient schemaRegistryClient;

  @Inject
  KafkaConfig kafkaConfig;

  @BeforeEach
  void setUp() throws IOException, RestClientException {

    final Deserializer<EVSpan> evSpanDeserializer =
        new SpecificAvroSerde<EVSpan>(schemaRegistryClient).deserializer();

    evSpanDeserializer.configure(
        Map.of(AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, "http://dummy"), false);

    SpanTranslator translator = new SpanTranslator(schemaRegistryClient, kafkaConfig);
    Topology topology = translator.getTopology();

    Properties props = new Properties();
    props.put(StreamsConfig.APPLICATION_ID_CONFIG, "test");
    props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "dummy:1234");


    driver = new TopologyTestDriver(topology, props);

    inputTopic = driver.createInputTopic("cluster-dump-spans", Serdes.ByteArray().serializer(),
        Serdes.ByteArray().serializer());
    outputTopic = driver.createOutputTopic("explorviz-spans", Serdes.String().deserializer(),
        evSpanDeserializer);


  }

  @AfterEach
  void tearDown() {
    driver.close();
  }

  public byte[] getDumpSpan() throws IOException {
    // Byte array containing a dumpspan of 50 spans
    final URL dumspan = getClass().getClassLoader().getResource("dumpspan50");
    if (dumspan == null) {
      throw new NullPointerException();
    }

    FileInputStream fis = new FileInputStream(dumspan.getFile());
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    bos.writeBytes(fis.readAllBytes());

    return bos.toByteArray();
  }

  @Test
  void testTranslationMultiple() throws IOException {
    byte[] dumpbytes = getDumpSpan();
    DumpSpans dump = DumpSpans.parseFrom(dumpbytes);
    byte[] id = dump.getSpans(0).getSpanId().toByteArray();

    inputTopic.pipeInput(id, dumpbytes);

    assertEquals(dump.getSpansList().size(), outputTopic.readValuesToList().size());
  }

  @Test
  void testTranslation() throws IOException {
    byte[] dumpbytes = getDumpSpan();
    DumpSpans dump = DumpSpans.parseFrom(dumpbytes);

    Span s = dump.getSpans(0);

    DumpSpans singleSpanDump = DumpSpans.newBuilder().addSpans(s).build();
    inputTopic.pipeInput(s.getSpanId().toByteArray(), singleSpanDump.toByteArray());

    EVSpan result = outputTopic.readKeyValue().value;

    String expectedTraceId = "50c246ad9c9883d1558df9f19b9ae7a6";
    String expectedSpanId = "7ef83c66eabd5fbb";
    long expectedStartTime = 1581938395702319100L;
    long expectedEndTime = 1581938395705981005L;
    String expectedAppName = "UNKNOWN-APPLICATION";
    String expectedOperationName =
        "net.explorviz.sampleApplication.database.helper.SQLConnectionHandler.createDatabase";

    // Check IDs
    assertEquals(expectedSpanId, result.getSpanId());
    assertEquals(expectedTraceId, result.getTraceId());

    // Start and End time


    assertEquals(expectedStartTime, result.getStartTime());
    assertEquals(expectedEndTime, (long) result.getEndTime());
    assertEquals(expectedOperationName, result.getOperationName());
    assertEquals(expectedAppName, result.getAppName());

  }



}
