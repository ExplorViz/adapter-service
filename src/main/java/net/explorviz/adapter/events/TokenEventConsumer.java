package net.explorviz.adapter.events;


import net.explorviz.avro.TokenEvent;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TokenEventConsumer {


  private static final Logger LOGGER = LoggerFactory.getLogger(TokenEventConsumer.class);

  @Incoming("token-events")
  public void process(TokenEvent event) {
    System.out.println("IM HEER");
    LOGGER.info("New event {}", event);
  }

}
