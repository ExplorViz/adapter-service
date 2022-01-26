package net.explorviz.adapter.events;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import net.explorviz.adapter.service.TokenService;
import net.explorviz.avro.TokenEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Kafka consumer which reads {@link TokenEvent} and passes them to the {@link TokenService}.
 */
@ApplicationScoped
public class TokenEventConsumer {

  private static final Logger LOGGER = LoggerFactory.getLogger(TokenEventConsumer.class);

  private final TokenService tokenService;

  @Inject
  public TokenEventConsumer(final TokenService tokenService) {
    this.tokenService = tokenService;
  }

  public void process(final TokenEvent event) {

    if (LOGGER.isTraceEnabled()) {
      LOGGER.trace("Received event {}", event);
    }

    switch (event.getType()) {
      case CREATED:
        this.tokenService.add(event.getToken());
        break;
      case DELETED:
        this.tokenService.delete(event.getToken());
        break;
      default:
        // Irrelevant event, do nothing
        break;
    }
  }


}
