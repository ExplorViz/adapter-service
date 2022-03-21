package net.explorviz.adapter.injection;

import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient;
import io.quarkus.arc.DefaultBean;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import net.explorviz.avro.TokenEvent;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StoreQueryParameters;
import org.apache.kafka.streams.errors.InvalidStateStoreException;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;

/**
 * Returns a {@link SchemaRegistryClient} that is used by this application with a maximum number of
 * 10 schemas.
 */
public class ReadOnlyKeyValueStoreProducer {

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
        // ignore, store not ready yet
      }
    }
  }
}

