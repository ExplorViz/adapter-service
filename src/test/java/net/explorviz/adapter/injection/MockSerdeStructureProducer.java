package net.explorviz.adapter.injection;

import io.confluent.kafka.schemaregistry.avro.AvroSchema;
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.rest.exceptions.RestClientException;
import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import io.quarkus.arc.profile.IfBuildProfile;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import java.io.IOException;
import java.util.Map;
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

    this.registry.register(this.outTopicSpans + "-value",
        new AvroSchema(net.explorviz.avro.Span.SCHEMA$));

    final SpecificAvroSerde<net.explorviz.avro.Span> valueSerde =
        new SpecificAvroSerde<>(this.registry);
    valueSerde.configure(
        Map.of(AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, "http://registry:1234"),
        false);
    return valueSerde;
  }
}

