package net.explorviz.adapter.service;

import io.quarkus.redis.client.RedisClient;
import io.quarkus.redis.client.reactive.ReactiveRedisClient;
import io.smallrye.mutiny.subscription.Cancellable;
import io.vertx.mutiny.redis.client.Response;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import net.explorviz.avro.LandscapeToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service to access available landscape tokens, backed by redis.
 */
@ApplicationScoped
public class TokenService {

  public static final Logger LOGGER = LoggerFactory.getLogger(TokenService.class);

  private final RedisClient redisClient;
  private final ReactiveRedisClient reactiveRedisClient;

  @Inject
  public TokenService(final RedisClient redisClient,
                      final ReactiveRedisClient reactiveRedisClient) {
    this.redisClient = redisClient;
    this.reactiveRedisClient = reactiveRedisClient;
  }

  /**
   * Adds a token to the set of valid token. Non-blocking.
   *
   * @param token the token to add.
   * @return Uni future of the response
   */
  public Cancellable add(final LandscapeToken token) {
    return this.add(token,
        item -> {
          if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("Added token {}", token);
          }
        },
        error -> {
          if (LOGGER.isErrorEnabled()) {
            LOGGER.error("Failed to add token {}: {}", token, error.toString());
          }
        });
  }

  /**
   * Adds a token to the set of valid token. Non-blocking.
   *
   * @param token the token to add.
   * @return Uni future of the response
   */
  public Cancellable add(final LandscapeToken token, final Consumer<? super Response> onItem,
                         final Consumer<Throwable> onError) {
    if (LOGGER.isTraceEnabled()) {
      LOGGER.trace("Adding token {} non-blocking", token);
    }
    final List<String> entry = Arrays.asList(token.getValue(), token.getSecret());
    return this.reactiveRedisClient.set(entry).subscribe().with(onItem, onError);
  }

  /**
   * Adds a token to the set of valid token. Blocking until completed.
   *
   * @param token the token to add.
   */
  public void addBlocking(final LandscapeToken token) {
    if (LOGGER.isTraceEnabled()) {
      LOGGER.trace("Adding token {}", token);
    }
    final List<String> entry = Arrays.asList(token.getValue(), token.getSecret());
    this.redisClient.set(entry);
  }

  /**
   * Removes a token from the set of valid tokens, if it is contained. Non-blocking.
   *
   * @param token the token to remove
   * @return Uni future of the response
   */
  public Cancellable delete(final LandscapeToken token, final Consumer<? super Response> onItem,
                            final Consumer<Throwable> onError) {
    return this.reactiveRedisClient.del(Collections.singletonList(token.getValue())).subscribe()
        .with(onItem, onError);
  }

  /**
   * Removes a token from the set of valid tokens, if it is contained. Non-blocking.
   *
   * @param token the token to remove
   * @return Uni future of the response
   */
  public Cancellable delete(final LandscapeToken token) {
    return this.delete(token,
        item -> {
          if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("Deleted token {}", token);
          }
        },
        error -> {
          if (LOGGER.isErrorEnabled()) {
            LOGGER.error("Failed to delete token {}: {}", token, error.toString());
          }
        });
  }

  /**
   * Removes a token from the set of valid tokens, if it is contained. Blocking until completed.
   *
   * @param token the token to remove
   */
  public void deleteBlocking(final LandscapeToken token) {
    this.reactiveRedisClient.delAndAwait(Collections.singletonList(token.getValue()));
  }

  /**
   * Checks whether a given token exists.
   *
   * @param tokenValue the token to check
   * @return {@code true} iff the given token is in the list of valid tokens, {@code false}
   *     otherwise.
   */
  public boolean exists(final String tokenValue) {
    return this.redisClient.exists(Collections.singletonList(tokenValue)).toInteger() == 1;
  }

  /**
   * Checks whether a given token exists.
   *
   * @param tokenValue the token to check
   * @return {@code true} iff the given token is in the list of valid tokens, {@code false}
   *     otherwise.
   */
  public Optional<String> getSecret(final String tokenValue) {
    try {
      return Optional.of(this.redisClient.get(tokenValue).toString());
    } catch (NullPointerException npe) { // NOPMD
      return Optional.empty();
    }
  }



}
