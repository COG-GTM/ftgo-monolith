package net.chrisrichardson.ftgo.consumerservice.api.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class CreateConsumerResponseTest {

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Test
  public void noArgConstructorDefaultsConsumerIdToZero() {
    assertEquals(0L, new CreateConsumerResponse().getConsumerId());
  }

  @Test
  public void constructorAndSetterStoreConsumerId() {
    CreateConsumerResponse response = new CreateConsumerResponse(42L);
    assertEquals(42L, response.getConsumerId());

    response.setConsumerId(7L);
    assertEquals(7L, response.getConsumerId());
  }

  @Test
  public void roundTripsThroughJson() throws Exception {
    String json = objectMapper.writeValueAsString(new CreateConsumerResponse(42L));

    assertEquals("{\"consumerId\":42}", json);
    assertEquals(42L, objectMapper.readValue(json, CreateConsumerResponse.class).getConsumerId());
  }
}
