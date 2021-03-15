package net.explorviz.adapter;

import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.annotations.QuarkusMain;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import javax.inject.Inject;
import net.explorviz.adapter.conversion.ConversionStream;
import net.explorviz.adapter.injection.KafkaConfig;
import org.apache.kafka.clients.admin.Admin;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.KafkaAdminClient;
import org.apache.kafka.streams.KafkaStreams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Entry point. Runs the Quarkus application.
 */
@QuarkusMain
public class Main {

  public static void main(String[] args) {
    Quarkus.run(AdapterServiceApplication.class);
  }


  /**
   * The Adapter-Service Quarkus application.
   */
  public static class AdapterServiceApplication implements QuarkusApplication {

    private static final Logger LOGGER = LoggerFactory.getLogger(QuarkusApplication.class);

    @Inject
    KafkaConfig config;

    @Inject
    ConversionStream stream;

    @Override
    public int run(final String... args) throws Exception {
      LOGGER.info("Healthcheck...");
      healthcheck();
      LOGGER.info("Ready, starting stream processor...");
      startStream();
      Quarkus.waitForExit();
      return 0;
    }

    private void startStream() {
      KafkaStreams streams = new KafkaStreams(stream.getTopology(), stream.getStreamsConfig());
      streams.cleanUp();
      streams.start();
    }

    private void healthcheck() throws InterruptedException, ExecutionException {

      final Properties props = new Properties();
      props.put("bootstrap.servers", config.getBootstrapServers());
      Admin kAdm = KafkaAdminClient.create(props);

      boolean healthy = false;

      while (!healthy) {
        int sleep = 2_000;
        Set<String> topics = null;
        try {
          topics = kAdm.listTopics().names().get(2, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
          LOGGER.warn("Could not establish connection to Kafka, trying again in 2 seconds");
        }

        if (topics != null) {
          // Got topics, check if required topics are available
          if (!topics.contains(config.getInTopic())
              || !topics.contains(config.getDynamicOutTopic())
              || !topics.contains(config.getStructureOutTopic())) {
            LOGGER.warn("Topics not (yet) created, trying again in 2 seconds");
          } else {
            healthy = true;
          }
        }

        Thread.sleep(sleep);

      }



    }

  }

}
