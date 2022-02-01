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
import net.explorviz.avro.TokenEvent;
import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 * Returns an injectable {@link SpecificAvroSerde}.
 */
@Dependent
public class MockSerdeTokenEventProducer {

  @ConfigProperty(name = "explorviz.kafka-streams.topics.out.dynamic")
  /* default */ String outTopicDynamic; // NOCS

  @Inject
  /* default */ SchemaRegistryClient registry; // NOCS

  @Produces
  @IfBuildProfile("test")
  public SpecificAvroSerde<TokenEvent> produceMockSpecificAvroSerde()
      throws IOException, RestClientException {

    this.registry.register("token-events" + "-value", TokenEvent.SCHEMA$);

    final SpecificAvroSerde<TokenEvent> valueSerde = new SpecificAvroSerde<>(this.registry);
    valueSerde.configure(
        Map.of(AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, "http://registry:1234"),
        false);
    return valueSerde;
  }
}

