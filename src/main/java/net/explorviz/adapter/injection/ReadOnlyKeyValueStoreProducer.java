package net.explorviz.adapter.injection;

import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient;
import io.quarkus.arc.DefaultBean;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import net.explorviz.avro.TokenEvent;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StoreQueryParameters;
import org.apache.kafka.streams.errors.InvalidStateStoreException;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Returns a {@link SchemaRegistryClient} that is used by this application with a maximum number of
 * 10 schemas.
 */
public class ReadOnlyKeyValueStoreProducer {

  private static final Logger LOGGER = LoggerFactory.getLogger(ReadOnlyKeyValueStoreProducer.class);

  @Inject
  /* default */ KafkaStreams streams; // NOCS

  @Produces
  @DefaultBean
  public ReadOnlyKeyValueStore<String, TokenEvent> getTokenEventStore() {
    while (true) {
      try {
        return this.streams.store(StoreQueryParameters.fromNameAndType("token-events-global-store",
            QueryableStoreTypes.keyValueStore()));
      } catch (final InvalidStateStoreException e) {
        LOGGER.debug("State store not yet initialized, will try again...");
      }
    }
  }
}

