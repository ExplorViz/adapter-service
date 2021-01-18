package net.explorviz.adapter.injection;

import javax.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class KafkaConfig {

  // CHECKSTYLE:OFF

  @ConfigProperty(name = "quarkus.kafka-streams.application-id") // NOPMD
  /* default */ String applicationId;

  @ConfigProperty(name = "quarkus.kafka-streams.bootstrap-servers") // NOPMD
  /* default */ String bootstrapServers;

  @ConfigProperty(name = "quarkus.kafka-streams.topics") // NOPMD
  /* default */ String inTopic;

  @ConfigProperty(name = "explorviz.kafka-streams.topics.out.structure") // NOPMD
  /* default */ String structureOutTopic;

  @ConfigProperty(name = "explorviz.kafka-streams.topics.out.dynamic") // NOPMD
  /* default */ String dynamicOutTopic;

  @ConfigProperty(name = "explorviz.schema-registry.url") // NOPMD
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
