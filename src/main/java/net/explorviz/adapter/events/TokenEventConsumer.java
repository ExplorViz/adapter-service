package net.explorviz.adapter.events;


import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import net.explorviz.adapter.service.TokenService;
import net.explorviz.avro.TokenEvent;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@ApplicationScoped
public class TokenEventConsumer {

  private static final Logger LOGGER = LoggerFactory.getLogger(TokenEventConsumer.class);

  private TokenService tokenService;

  @Inject
  public TokenEventConsumer(final TokenService tokenService) {
    this.tokenService = tokenService;
  }

  @Incoming("token-events")
  public void process(TokenEvent event) {
    switch (event.getType()) {
      case CREATED:
        onTokenCreated(event.getToken());
      case DELETED:
        onTokenDelete(event.getToken());
        break;
      default:
        // Irrelevant event, do nothing
        break;
    }
  }

  private void onTokenDelete(String token) {
    tokenService.delete(token).onItem().invoke(
        () -> LOGGER.info("Deleted token {}", token)
    );
  }

  private void onTokenCreated(String token) {
    tokenService.add(token).onItem().invoke(
        () -> LOGGER.info("Deleted token {}", token)
    );
  }


}
