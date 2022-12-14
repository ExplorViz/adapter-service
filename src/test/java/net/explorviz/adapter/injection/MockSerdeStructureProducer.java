package net.explorviz.adapter.injection;

import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.rest.exceptions.RestClientException;
import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import io.quarkus.arc.profile.IfBuildProfile;
import java.io.IOException;
import java.util.Map;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 * Returns an injectable {@link SpecificAvroSerde}.
 */
@Dependent
public class MockSerdeStructureProducer {

  @ConfigProperty(name = "explorviz.kafka-streams.topics.out.spans")
  /* default */ String outTopicSpans; // NOCS

  @Inject
  /* default */ SchemaRegistryClient registry; // NOCS

  @Produces
  @IfBuildProfile("test")
  public SpecificAvroSerde<net.explorviz.avro.Span> produceMockSpecificAvroSerde()
      throws IOException, RestClientException {

    this.registry.register(this.outTopicSpans + "-value", net.explorviz.avro.Span.SCHEMA$);

    final SpecificAvroSerde<net.explorviz.avro.Span> valueSerde = new SpecificAvroSerde<>(this.registry);
    valueSerde.configure(
        Map.of(AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, "http://registry:1234"),
        false);
    return valueSerde;
  }
}

