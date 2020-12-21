package net.explorviz.adapter.events;


import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.groups.UniSubscribe;
import io.vertx.mutiny.redis.client.Response;
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
    LOGGER.info("Received event {}", event);
    switch (event.getType()) {
      case CREATED:
        tokenService.add(event.getToken());
        break;
      case DELETED:
        tokenService.delete(event.getToken());
        break;
      default:
        // Irrelevant event, do nothing
        break;
    }
  }


}
