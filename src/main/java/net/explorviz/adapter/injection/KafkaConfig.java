package net.explorviz.adapter.injection;

import javax.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 * Wrapper class for ConfigProperties that are read out of the application.properties file.
 */
@ApplicationScoped
@SuppressWarnings("PMD.DefaultPackage")
public class KafkaConfig {

  // CHECKSTYLE:OFF

  @ConfigProperty(name = "quarkus.kafka-streams.application-id")
  /* default */ String applicationId;

  @ConfigProperty(name = "quarkus.kafka-streams.bootstrap-servers")
  /* default */ String bootstrapServers;

  @ConfigProperty(name = "quarkus.kafka-streams.topics")
  /* default */ String inTopic;

  @ConfigProperty(name = "explorviz.kafka-streams.topics.out.structure")
  /* default */ String structureOutTopic;

  @ConfigProperty(name = "explorviz.kafka-streams.topics.out.dynamic")
  /* default */ String dynamicOutTopic;

  @ConfigProperty(name = "explorviz.schema-registry.url")
  /* default */ String schemaRegistryUrl;

  // CHECKSTYLE:ON

  public String getApplicationId() {
    return this.applicationId;
  }

  public String getBootstrapServers() {
    return this.bootstrapServers;
  }

  public String getInTopic() {
    return this.inTopic;
  }

  public String getStructureOutTopic() {
    return this.structureOutTopic;
  }

  public String getDynamicOutTopic() {
    return this.dynamicOutTopic;
  }

  public String getSchemaRegistryUrl() {
    return this.schemaRegistryUrl;
  }

}
