package net.explorviz.adapter.injection;

import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import io.quarkus.arc.DefaultBean;
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

  @ConfigProperty(name = "explorviz.schema-registry.url")
  /* default */ String schemaRegistryUrl; // NOCS

  @Produces
  @DefaultBean
  public <T extends SpecificRecord> SpecificAvroSerde<T> produceSpecificAvroSerde() {
    return new SpecificAvroSerde<>(this.registry);
  }
}

