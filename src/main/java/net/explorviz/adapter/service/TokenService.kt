package net.explorviz.adapter.service;


import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import net.explorviz.avro.TokenEvent;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;

/**
 * Service to access available landscape tokens, backed by GlobalKTable and state store access.
 */
@ApplicationScoped
class TokenService @Inject constructor(
  private val keyValueStore: ReadOnlyKeyValueStore<String, TokenEvent>
) {

  /**
   * Checks whether a given token exists.
   *
   * @param tokenValue the token to check
   * @return `true` if the given token value is in the list of valid tokens
   */
  fun validLandscapeTokenValue(tokenValue: String): Boolean {
    val potentialEvent = keyValueStore[tokenValue]
    return potentialEvent?.token?.value == tokenValue
  }

  /**
   * Checks whether a given landscape token and secret exist and belong to each other.
   *
   * @param tokenValue the token to check
   * @param tokenSecret the secret to check
   * @return `true` if the given token value is valid and the secret belongs to this token.
   */
  fun validLandscapeTokenValueAndSecret(tokenValue: String, tokenSecret: String): Boolean {
    val potentialEvent = keyValueStore[tokenValue]
    return potentialEvent?.token?.value == tokenValue &&
        potentialEvent.token.secret == tokenSecret
  }
}
