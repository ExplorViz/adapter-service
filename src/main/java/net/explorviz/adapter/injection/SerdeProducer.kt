package net.explorviz.adapter.injection;

import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient;
import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import io.quarkus.arc.profile.UnlessBuildProfile
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import org.apache.avro.specific.SpecificRecord;
import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 * Returns an injectable [SpecificAvroSerde].
 */
@Dependent
class SerdeProducer {

  @Inject
  /* default */  lateinit var registry: SchemaRegistryClient

  @ConfigProperty(name = "quarkus.kafka-streams.schema-registry-url")
  lateinit var schemaRegistryUrl: String

  @Produces
  @jakarta.enterprise.inject.Default
  @UnlessBuildProfile("test")
  fun <T : SpecificRecord> produceSpecificAvroSerde(): SpecificAvroSerde<T> {
    val valueSerde = SpecificAvroSerde<T>(registry)
    valueSerde.configure(
      mapOf(AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG to schemaRegistryUrl),
      false
    )
    return valueSerde
  }
}

