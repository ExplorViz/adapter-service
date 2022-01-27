package net.explorviz.adapter.service;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import net.explorviz.avro.TokenEvent;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StoreQueryParameters;
import org.apache.kafka.streams.errors.InvalidStateStoreException;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service to access available landscape tokens, backed by GlobalKTable and state store access.
 */
@ApplicationScoped
public class TokenService {

  public static final Logger LOGGER = LoggerFactory.getLogger(TokenService.class);

  @Inject
  /* default */ KafkaStreams streams; // NOCS

  /**
   * Checks whether a given token exists.
   *
   * @param tokenValue the token to check
   * @return {@code true} if the given token value is in the list of valid tokens, {@code false}
   *         otherwise.
   */
  public boolean validLandscapeTokenValue(final String tokenValue) {

    final TokenEvent potentialEvent = this.getTokenEventStore().get(tokenValue);

    if (potentialEvent != null) {
      return potentialEvent.getToken().getValue().equals(tokenValue);
    }

    return false;
  }

  /**
   * Checks whether a given landscape token and secret exist and belong to each other.
   *
   * @param tokenValue the token to check * @param tokenSecret the secret to check
   * @return {@code true} if the given token value is in the list of valid tokens and the given
   *         secret belongs to this token value, {@code false} otherwise.
   */
  public boolean validLandscapeTokenValueAndSecret(final String tokenValue,
      final String tokenSecret) {
    final TokenEvent potentialEvent = this.getTokenEventStore().get(tokenValue);

    if (potentialEvent != null) {
      return potentialEvent.getToken().getValue().equals(tokenValue)
          && potentialEvent.getToken().getSecret().equals(tokenSecret);
    }

    return false;
  }

  private ReadOnlyKeyValueStore<String, TokenEvent> getTokenEventStore() {
    while (true) {
      try {
        return this.streams.store(StoreQueryParameters.fromNameAndType("token-events-global-store",
            QueryableStoreTypes.keyValueStore()));
      } catch (final InvalidStateStoreException e) { // NOPMD
        // ignore, store not ready yet
      }
    }
  }

}
