package net.explorviz.adapter.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.quarkus.test.junit.QuarkusTest;
import java.time.Duration;
import javax.inject.Inject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import redis.embedded.RedisServer;

@QuarkusTest
class TokenServiceTest {



  @Inject
  TokenService service;

  RedisServer redisServer;

  @BeforeEach
  void setUp() {
    redisServer = new RedisServer(6379);
    redisServer.start();
  }

  @AfterEach
  void tearDown() {
    redisServer.stop();
  }

  @Test
  synchronized void testAdd() {
    final String tokenToAdd = "123456789";
    service.add(tokenToAdd).await().atMost(Duration.ofMillis(100));
    assertTrue(service.exists(tokenToAdd));
  }

  @Test
  void testNonExisting() {
    final String token = "123456789";
    assertFalse(service.exists(token));
  }

  @Test
  void testDelete() {
    final String token = "123456789";
    service.addBlocking(token); // Make sure key was actually added
    service.delete(token).await().atMost(Duration.ofMillis(100));
    assertFalse(service.exists(token));
  }

}
