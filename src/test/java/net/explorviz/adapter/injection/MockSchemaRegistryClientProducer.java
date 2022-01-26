package net.explorviz.adapter.injection;

import io.confluent.kafka.schemaregistry.client.MockSchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient;
import io.quarkus.arc.profile.IfBuildProfile;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Produces;

@Dependent
public class MockSchemaRegistryClientProducer {

  @Produces
  @IfBuildProfile("test")
  public SchemaRegistryClient produceMockSchemaRegistry() {
    return new MockSchemaRegistryClient();
  }

}
