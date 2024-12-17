package net.explorviz.adapter.injection

import io.quarkus.arc.DefaultBean
import jakarta.enterprise.context.Dependent
import jakarta.enterprise.inject.Produces
import jakarta.inject.Inject
import net.explorviz.avro.TokenEvent
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.StoreQueryParameters
import org.apache.kafka.streams.errors.InvalidStateStoreException
import org.apache.kafka.streams.state.QueryableStoreTypes
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/** Returns a {@link SchemaRegistryClient} that is used by this application with a maximum number of 10 schemas. */
@Dependent
class ReadOnlyKeyValueStoreProducer {

    companion object {
        private val LOGGER: Logger = LoggerFactory.getLogger(ReadOnlyKeyValueStoreProducer::class.java)
    }

    @Inject lateinit var streams: KafkaStreams

    @Produces
    @DefaultBean
    fun getTokenEventStore(): ReadOnlyKeyValueStore<String, TokenEvent> {
        while (true) {
            try {
                return streams.store(
                    StoreQueryParameters.fromNameAndType(
                        "token-events-global-store",
                        QueryableStoreTypes.keyValueStore()
                    )
                )
            } catch (e: InvalidStateStoreException) {
                LOGGER.debug("State store not yet initialized, will try again...")
            }
        }
    }
}
