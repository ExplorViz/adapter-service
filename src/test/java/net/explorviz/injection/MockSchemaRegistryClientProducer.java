package net.explorviz.injection;

import io.confluent.kafka.schemaregistry.client.MockSchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient;
import io.quarkus.test.Mock;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Produces;

@Mock
@Dependent
public class MockSchemaRegistryClientProducer extends SchemaRegistryClientProducer {
  
  @Override
  @Produces
  public SchemaRegistryClient schemaRegistryClient() {
    return new MockSchemaRegistryClient();
  }
}
