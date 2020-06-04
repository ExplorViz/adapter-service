package net.explorviz.kafka;

import static org.junit.jupiter.api.Assertions.assertEquals;
import io.confluent.kafka.schemaregistry.client.MockSchemaRegistryClient;
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

  private TestInputTopic<byte[], byte[]> inputTopic;
  private TestOutputTopic<String, EVSpan> outputTopic;

  private SpecificAvroSerde<EVSpan> evSpanSerDe;

  @Inject
  KafkaConfig config;

  @BeforeEach
  void setUp() throws IOException, RestClientException {

    final SchemaRegistryClient schemaRegistryClient = new MockSchemaRegistryClient();

    final Topology topology = new SpanTranslator(schemaRegistryClient, this.config).getTopology();

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

  private byte[] getDumpSpan() throws IOException {
    // Byte array containing a dumpspan of 50 spans
    final URL dumspan = this.getClass().getClassLoader().getResource("dumpspan50");
    if (dumspan == null) {
      throw new NullPointerException();
    }

    final FileInputStream fis = new FileInputStream(dumspan.getFile());
    final ByteArrayOutputStream bos = new ByteArrayOutputStream();
    bos.writeBytes(fis.readAllBytes());

    fis.close();

    return bos.toByteArray();
  }

  @Test
  void testTranslation() throws IOException {
    final byte[] dumpbytes = this.getDumpSpan();
    final DumpSpans dump = DumpSpans.parseFrom(dumpbytes);

    final Span s = dump.getSpans(0);

    final DumpSpans singleSpanDump = DumpSpans.newBuilder().addSpans(s).build();
    this.inputTopic.pipeInput(s.getSpanId().toByteArray(), singleSpanDump.toByteArray());

    final EVSpan result = this.outputTopic.readKeyValue().value;

    final String expectedTraceId = "50c246ad9c9883d1558df9f19b9ae7a6";
    final String expectedSpanId = "7ef83c66eabd5fbb";
    final Instant expectedStartTime = Instant.ofEpochSecond(1581938395, 702319100L);
    final long expectedEndTime = 1581938395705L;
    final String expectedAppName = "UNKNOWN-APPLICATION";
    final String expectedOperationName =
        "net.explorviz.sampleApplication.database.helper.SQLConnectionHandler.createDatabase";

    // Check IDs
    assertEquals(expectedSpanId, result.getSpanId());
    assertEquals(expectedTraceId, result.getTraceId());

    // Start and End time
    assertEquals(expectedStartTime, Instant.ofEpochSecond(result.getStartTime().getSeconds(),
        result.getStartTime().getNanoAdjust()));
    assertEquals(expectedEndTime, (long) result.getEndTime());
    assertEquals(expectedOperationName, result.getOperationName());
    assertEquals(expectedAppName, result.getAppName());
  }

  @Test
  void testTranslationMultiple() throws IOException {
    final byte[] dumpbytes = this.getDumpSpan();
    final DumpSpans dump = DumpSpans.parseFrom(dumpbytes);
    final byte[] id = dump.getSpans(0).getSpanId().toByteArray();

    this.inputTopic.pipeInput(id, dumpbytes);

    assertEquals(dump.getSpansList().size(), this.outputTopic.readValuesToList().size());
  }

}
