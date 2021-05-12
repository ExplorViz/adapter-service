package net.explorviz.adapter.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.quarkus.test.junit.QuarkusTest;
import java.util.Optional;
import javax.inject.Inject;
import net.explorviz.avro.LandscapeToken;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import redis.embedded.RedisServer;

@QuarkusTest
class TokenServiceTest {

  @Inject
  TokenService service;

  RedisServer redisServer;

  private LandscapeToken token = LandscapeToken.newBuilder()
      .setValue("123456789")
      .setAlias("Alias")
      .setSecret("Secret")
      .setCreated(0)
      .setOwnerId("user123").build();

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

    this.service.addBlocking(token);
    assertTrue(this.service.exists(token.getValue()));
  }

  @Test
  synchronized void testAddNonBlocking() {
    this.service.add(token, i -> assertTrue(this.service.exists(token.getValue())), e -> {
    });
  }

  @Test
  void testGetSecret() {
    service.addBlocking(token);
    String got = service.getSecret(token.getValue()).get();
    assertEquals(token.getSecret(), got);
  }

  @Test
  void testGetSecretOfUnknown() {
    Optional<String> got = service.getSecret(token.getValue());
    assertTrue(got.isEmpty());
  }

  @Test
  void testNonExisting() {
    assertFalse(this.service.exists(token.getValue()));
  }

  @Test
  void testDelete() {
    this.service.addBlocking(token); // Make sure key was actually added
    this.service.deleteBlocking(token);
    assertFalse(this.service.exists(token.getValue()));
  }

  @Test
  void testDeleteNonBlocking() {
    this.service.addBlocking(token); // Make sure key was actually added
    this.service.delete(token, i -> assertFalse(this.service.exists(token.getValue())), e -> {
    });
  }

}
