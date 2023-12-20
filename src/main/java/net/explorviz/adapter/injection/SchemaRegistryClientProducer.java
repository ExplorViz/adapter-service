package net.explorviz.adapter.injection;

import io.confluent.kafka.schemaregistry.client.CachedSchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient;
import io.quarkus.arc.DefaultBean;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Produces;
import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 * Returns a {@link SchemaRegistryClient} that is used by this application with a maximum number of
 * 10 schemas.
 */
@Dependent
public class SchemaRegistryClientProducer {

  private static final int MAX_NUM_OF_SCHEMAS = 10;

  @ConfigProperty(name = "quarkus.kafka-streams.schema-registry-url")
  /* default */ String schemaRegistryUrl; // NOCS

  @Produces
  @DefaultBean
  public SchemaRegistryClient schemaRegistryClient() {
    return new CachedSchemaRegistryClient(this.schemaRegistryUrl, MAX_NUM_OF_SCHEMAS);
  }
}
