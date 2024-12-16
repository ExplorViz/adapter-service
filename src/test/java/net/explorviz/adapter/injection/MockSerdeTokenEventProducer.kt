package net.explorviz.adapter.injection;

import io.confluent.kafka.schemaregistry.avro.AvroSchema
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient
import io.confluent.kafka.schemaregistry.client.rest.exceptions.RestClientException
import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde
import io.quarkus.arc.profile.IfBuildProfile
import jakarta.enterprise.context.Dependent
import jakarta.enterprise.inject.Produces
import jakarta.inject.Inject
import net.explorviz.avro.TokenEvent
import org.apache.avro.Schema
import org.eclipse.microprofile.config.inject.ConfigProperty
import java.io.IOException

/**
 * Returns an injectable {@link SpecificAvroSerde}.
 */

@Dependent
class MockSerdeTokenEventProducer @Inject constructor(
  var registry: SchemaRegistryClient?,
  @ConfigProperty(name = "explorviz.kafka-streams.topics.in.tokens")
  var inTopicToken: String? = null
) {
  @Produces
  @IfBuildProfile("test")
  @Throws(IOException::class, RestClientException::class)
  fun produceMockSpecificAvroSerde(): SpecificAvroSerde<TokenEvent>? {
    return if (inTopicToken != null && registry != null) {
      registry?.register(
        "${inTopicToken}-value",
        AvroSchema(Schema.Parser().parse(net.explorviz.avro.TokenEvent.`SCHEMA$`.toString()))
      )

      val valueSerde = SpecificAvroSerde<TokenEvent>(registry)
      valueSerde.configure(
        mapOf(AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG to "http://registry:1234"),
        false
      )
      valueSerde
    } else {
      null
    }
  }
}
