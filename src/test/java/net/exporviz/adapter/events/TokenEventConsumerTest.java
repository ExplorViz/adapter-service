package net.exporviz.adapter.events;

import net.explorviz.adapter.events.TokenEventConsumer;
import net.explorviz.adapter.service.TokenService;
import net.explorviz.avro.EventType;
import net.explorviz.avro.TokenEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;

public class TokenEventConsumerTest {

  private TokenEventConsumer consumer;
  private TokenService service;
  private TokenEvent event;


  @BeforeEach
  void setup() {
    this.event = Mockito.mock(TokenEvent.class);
    this.service = Mockito.mock(TokenService.class);
    this.consumer = new TokenEventConsumer(this.service);
  }

  @Test
  void testCreatedCase() {
    final EventType eventType = EventType.CREATED;

    Mockito.when(this.event.getType()).thenReturn(eventType);

    // Test target method
    this.consumer.process(this.event);

    Mockito.verify(this.service, Mockito.times(1)).add(Matchers.any());
  }

  @Test
  void testDeletedCase() {
    final EventType eventType = EventType.DELETED;

    Mockito.when(this.event.getType()).thenReturn(eventType);

    // Test target method
    this.consumer.process(this.event);

    Mockito.verify(this.service, Mockito.times(1)).delete(Matchers.any());
  }

}
