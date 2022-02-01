package net.explorviz.adapter.injection;

import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient;
import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import io.quarkus.arc.DefaultBean;
import java.util.Map;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import org.apache.avro.specific.SpecificRecord;
import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 * Returns an injectable {@link SpecificAvroSerde}.
 */
@Dependent
public class SerdeProducer {

  @Inject
  /* default */ SchemaRegistryClient registry; // NOCS

  @ConfigProperty(name = "quarkus.kafka-streams.schema-registry-url")
  /* default */ String schemaRegistryUrl; // NOCS

  @Produces
  @DefaultBean
  public <T extends SpecificRecord> SpecificAvroSerde<T> produceSpecificAvroSerde() {
    final SpecificAvroSerde<T> valueSerde = new SpecificAvroSerde<>(this.registry);
    valueSerde.configure(
        Map.of(AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, this.schemaRegistryUrl),
        false);
    return valueSerde;

  }
}

