package net.explorviz.adapter.injection

import io.confluent.kafka.schemaregistry.client.CachedSchemaRegistryClient
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient
import io.quarkus.arc.DefaultBean
import jakarta.enterprise.context.Dependent
import jakarta.enterprise.inject.Produces
import org.eclipse.microprofile.config.inject.ConfigProperty

/** Returns a {@link SchemaRegistryClient} that is used by this application with a maximum number of 10 schemas. */
@Dependent
class SchemaRegistryClientProducer {

    companion object {
        private const val MAX_NUM_OF_SCHEMAS = 10
    }

    @ConfigProperty(name = "quarkus.kafka-streams.schema-registry-url") lateinit var schemaRegistryUrl: String

    @Produces
    @DefaultBean
    fun schemaRegistryClient(): SchemaRegistryClient {
        return CachedSchemaRegistryClient(schemaRegistryUrl, MAX_NUM_OF_SCHEMAS)
    }
}
