package net.chrisrichardson.ftgo.orderservice.api.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class CreateOrderResponseTest {

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Test
  public void constructorAndSetterHoldOrderId() {
    CreateOrderResponse response = new CreateOrderResponse(42L);
    assertEquals(42L, response.getOrderId());

    response.setOrderId(43L);
    assertEquals(43L, response.getOrderId());
  }

  @Test
  public void roundTripsThroughJson() throws Exception {
    String json = objectMapper.writeValueAsString(new CreateOrderResponse(99L));
    assertEquals("{\"orderId\":99}", json);

    assertEquals(99L, objectMapper.readValue(json, CreateOrderResponse.class).getOrderId());
  }
}
