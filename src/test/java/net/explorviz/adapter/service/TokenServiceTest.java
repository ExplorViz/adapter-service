package net.explorviz.adapter.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import io.quarkus.test.junit.QuarkusTest;
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
    this.redisServer = new RedisServer(6379); // NOCS
    this.redisServer.start();
  }

  @AfterEach
  void tearDown() {
    this.redisServer.stop();
  }

  @Test
  synchronized void testAdd() {
    final String tokenToAdd = "123456789";
    this.service.addBlocking(tokenToAdd);
    assertTrue(this.service.exists(tokenToAdd));
  }

  @Test
  synchronized void testAddNonBlocking() throws InterruptedException {
    final String tokenToAdd = "123456789";
    this.service.add(tokenToAdd, i -> assertTrue(this.service.exists(tokenToAdd)), e -> {
    });
  }

  @Test
  void testNonExisting() {
    final String token = "123456789";
    assertFalse(this.service.exists(token));
  }

  @Test
  void testDelete() {
    final String token = "123456789";
    this.service.addBlocking(token); // Make sure key was actually added
    this.service.deleteBlocking(token);
    assertFalse(this.service.exists(token));
  }

  @Test
  void testDeleteNonBlocking() {
    final String token = "123456789";
    this.service.addBlocking(token); // Make sure key was actually added
    this.service.delete(token, i -> assertFalse(this.service.exists(token)), e -> {
    });
  }

}
