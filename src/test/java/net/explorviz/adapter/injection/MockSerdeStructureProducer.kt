package net.explorviz.adapter.injection

import io.confluent.kafka.schemaregistry.avro.AvroSchema
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient
import io.confluent.kafka.schemaregistry.client.rest.exceptions.RestClientException
import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde
import io.quarkus.arc.profile.IfBuildProfile
import jakarta.enterprise.context.Dependent
import jakarta.enterprise.inject.Produces
import jakarta.inject.Inject
import java.io.IOException
import org.apache.avro.Schema
import org.eclipse.microprofile.config.inject.ConfigProperty

/** Returns an injectable [SpecificAvroSerde]. */
@Dependent
class MockSerdeStructureProducer
@Inject
constructor(
    var registry: SchemaRegistryClient?,
    @ConfigProperty(name = "explorviz.kafka-streams.topics.out.spans") var outTopicSpans: String? = null
) {

    @Produces
    @IfBuildProfile("test")
    @Throws(IOException::class, RestClientException::class)
    fun produceMockSpecificAvroSerde(): SpecificAvroSerde<net.explorviz.avro.Span> {
        registry?.register(
            "$outTopicSpans-value",
            AvroSchema(Schema.Parser().parse(net.explorviz.avro.Span.`SCHEMA$`.toString()))
        )

        val valueSerde = SpecificAvroSerde<net.explorviz.avro.Span>(registry)
        valueSerde.configure(
            mapOf(AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG to "http://registry:1234"),
            false
        )
        return valueSerde
    }
}
