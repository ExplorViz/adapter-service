package net.explorviz.adapter.injection;

import javax.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
// https://quarkus.io/guides/config#using-configproperties
public class KafkaConfig {

  @ConfigProperty(name = "quarkus.kafka-streams.application-id")
  String applicationId;

  @ConfigProperty(name = "quarkus.kafka-streams.bootstrap-servers")
  String bootstrapServers;

  @ConfigProperty(name = "quarkus.kafka-streams.topics")
  String inTopic;

  @ConfigProperty(name = "explorviz.kafka-streams.topics.out.structure")
  String structureOutTopic;

  @ConfigProperty(name = "explorviz.kafka-streams.topics.out.dynamic")
  String dynamicOutTopic;

  @ConfigProperty(name = "explorviz.schema-registry.url")
  String schemaRegistryUrl;

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
