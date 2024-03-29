package net.explorviz.adapter.service;


import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import net.explorviz.avro.TokenEvent;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;

/**
 * Service to access available landscape tokens, backed by GlobalKTable and state store access.
 */
@ApplicationScoped
public class TokenService {

  //public static final Logger LOGGER = LoggerFactory.getLogger(TokenService.class);

  private final ReadOnlyKeyValueStore<String, TokenEvent> keyValueStore;

  @Inject
  public TokenService(final ReadOnlyKeyValueStore<String, TokenEvent> keyValueStore) {
    this.keyValueStore = keyValueStore;
  }

  /**
   * Checks whether a given token exists.
   *
   * @param tokenValue the token to check
   * @return {@code true} if the given token value is in the list of valid tokens
   */
  public boolean validLandscapeTokenValue(final String tokenValue) {

    final TokenEvent potentialEvent = this.keyValueStore.get(tokenValue);

    if (potentialEvent != null) {
      return potentialEvent.getToken().getValue().equals(tokenValue);
    }

    return false;
  }

  /**
   * Checks whether a given landscape token and secret exist and belong to each other.
   *
   * @param tokenValue the token to check * @param tokenSecret the secret to check
   * @return {@code true} if the given token value is valid and the secret belongs to this token.
   */
  public boolean validLandscapeTokenValueAndSecret(final String tokenValue,
      final String tokenSecret) {
    final TokenEvent potentialEvent = this.keyValueStore.get(tokenValue);

    if (potentialEvent != null) {
      return potentialEvent.getToken().getValue().equals(tokenValue) && potentialEvent.getToken()
          .getSecret().equals(tokenSecret);
    }

    return false;
  }

}
