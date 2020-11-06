package net.explorviz.adapter.service;

import io.quarkus.redis.client.RedisClient;
import io.quarkus.redis.client.reactive.ReactiveRedisClient;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.redis.client.Response;
import java.util.Arrays;
import java.util.Collections;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

/**
 * Service to access available landscape tokens, backed by redis.
 */
@ApplicationScoped
public class TokenService {
  
  private RedisClient redisClient;
  private ReactiveRedisClient reactiveRedisClient;

  @Inject
  public TokenService(final RedisClient redisClient,
                      final ReactiveRedisClient reactiveRedisClient) {
    this.redisClient = redisClient;
    this.reactiveRedisClient = reactiveRedisClient;

    redisClient.ping(Collections.singletonList(""));
  }


  /**
   * Adds a token to the set of valid token. Non-blocking.
   *
   * @param token the token to add.
   * @return Uni future of the response
   */
  public Uni<Response> add(String token) {
    return reactiveRedisClient.set(Arrays.asList(token, ""));
  }

  /**
   * Adds a token to the set of valid token. Blocking until completed.
   *
   * @param token the token to add.
   */
  public void addBlocking(String token) {
    reactiveRedisClient.setAndAwait(Arrays.asList(token, ""));
  }

  /**
   * Removes a token from the set of valid tokens, if it is contained. Non-blocking.
   *
   * @param token the token to remove
   * @return Uni future of the response
   */
  public Uni<Response> delete(String token) {
    return reactiveRedisClient.del(Collections.singletonList(token));
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
