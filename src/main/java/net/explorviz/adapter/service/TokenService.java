package net.explorviz.adapter.service;

import io.quarkus.redis.client.RedisClient;
import io.quarkus.redis.client.reactive.ReactiveRedisClient;
import io.smallrye.mutiny.subscription.Cancellable;
import io.vertx.mutiny.redis.client.Response;
import java.util.Arrays;
import java.util.Collections;
import java.util.function.Consumer;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service to access available landscape tokens, backed by redis.
 */
@ApplicationScoped
public class TokenService {

  public static final Logger LOGGER = LoggerFactory.getLogger(TokenService.class);

  private RedisClient redisClient;
  private ReactiveRedisClient reactiveRedisClient;

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
  public Cancellable add(String token) {
    return add(token,
        item -> {
          if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Added token {}", token);
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
  public Cancellable add(String token, Consumer<? super Response> onItem,
                         Consumer<Throwable> onError) {
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("Adding token {} non-blocking", token);
    }    
    return reactiveRedisClient.set(Arrays.asList(token, "")).subscribe().with(onItem, onError);
  }

  /**
   * Adds a token to the set of valid token. Blocking until completed.
   *
   * @param token the token to add.
   */
  public void addBlocking(String token) {
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("Adding token {}", token);
    }
    redisClient.set(Arrays.asList(token, ""));
  }

  /**
   * Removes a token from the set of valid tokens, if it is contained. Non-blocking.
   *
   * @param token the token to remove
   * @return Uni future of the response
   */
  public Cancellable delete(String token, Consumer<? super Response> onItem,
                            Consumer<Throwable> onError) {
    return reactiveRedisClient.del(Collections.singletonList(token)).subscribe()
        .with(onItem, onError);
  }

  /**
   * Removes a token from the set of valid tokens, if it is contained. Non-blocking.
   *
   * @param token the token to remove
   * @return Uni future of the response
   */
  public Cancellable delete(String token) {
    return delete(token,
        item -> {
          if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Deleted token {}", token);
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
  public void deleteBlocking(String token) {
    reactiveRedisClient.delAndAwait(Collections.singletonList(token));
  }

  /**
   * Checks whether a given token exists.
   *
   * @param token the token to check
   * @return {@code true} iff the given token is in the list of valid tokens,
   *     {@code false} otherwise.
   */
  public boolean exists(String token) {
    return redisClient.exists(Collections.singletonList(token)).toInteger() == 1;
  }



}
