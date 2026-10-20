package net.chrisrichardson.ftgo.orderservice.api.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class ReviseOrderRequestTest {

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Test
  public void constructorAndSetterHoldQuantities() {
    Map<String, Integer> quantities = Collections.singletonMap("1", 3);
    ReviseOrderRequest request = new ReviseOrderRequest(quantities);
    assertEquals(quantities, request.getRevisedLineItemQuantities());

    Map<String, Integer> replacement = new HashMap<>();
    replacement.put("2", 5);
    request.setRevisedLineItemQuantities(replacement);
    assertEquals(Integer.valueOf(5), request.getRevisedLineItemQuantities().get("2"));
    assertEquals(1, request.getRevisedLineItemQuantities().size());
  }

  @Test
  public void deserializesFromJsonUsingPrivateNoArgConstructor() throws Exception {
    ReviseOrderRequest request = objectMapper.readValue(
            "{\"revisedLineItemQuantities\":{\"1\":2,\"3\":0}}", ReviseOrderRequest.class);

    assertEquals(2, request.getRevisedLineItemQuantities().size());
    assertEquals(Integer.valueOf(2), request.getRevisedLineItemQuantities().get("1"));
    assertEquals(Integer.valueOf(0), request.getRevisedLineItemQuantities().get("3"));
  }

  @Test
  public void deserializingEmptyObjectLeavesQuantitiesNull() throws Exception {
    assertNull(objectMapper.readValue("{}", ReviseOrderRequest.class).getRevisedLineItemQuantities());
  }

  @Test
  public void serializesQuantities() throws Exception {
    JsonNode node = objectMapper.readTree(objectMapper.writeValueAsString(
            new ReviseOrderRequest(Collections.singletonMap("menu-1", 4))));

    assertEquals(4, node.get("revisedLineItemQuantities").get("menu-1").asInt());
  }
}
