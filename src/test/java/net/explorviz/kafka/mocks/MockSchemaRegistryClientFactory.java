package net.explorviz.kafka.mocks;

import io.confluent.kafka.schemaregistry.client.MockSchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.rest.exceptions.RestClientException;
import io.quarkus.test.Mock;
import java.io.IOException;
import javax.annotation.Priority;
import javax.enterprise.inject.Alternative;
import javax.enterprise.inject.Produces;
import net.explorviz.avro.EVSpan;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@Mock
@Alternative
@Priority(1)
public class MockSchemaRegistryClientFactory {

  @ConfigProperty(name = "explorviz.schema-registry.url")
  String schemaRegistryUrl;

  @Produces
  public SchemaRegistryClient schemaRegistryClient() {
    final MockSchemaRegistryClient schemaRegistryClient = new MockSchemaRegistryClient();
    try {
      schemaRegistryClient.register("explorviz-spans" + "-value", EVSpan.SCHEMA$);
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (RestClientException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return schemaRegistryClient;
  }
}
