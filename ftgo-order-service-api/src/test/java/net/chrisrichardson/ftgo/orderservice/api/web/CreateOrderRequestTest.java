package net.chrisrichardson.ftgo.orderservice.api.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class CreateOrderRequestTest {

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Test
  public void constructorAssignsConsumerAndRestaurantToTheRightFields() {
    CreateOrderRequest.LineItem lineItem = new CreateOrderRequest.LineItem("1", 2);
    CreateOrderRequest request = new CreateOrderRequest(11L, 22L, Collections.singletonList(lineItem));

    assertEquals(11L, request.getConsumerId());
    assertEquals(22L, request.getRestaurantId());
    assertEquals(1, request.getLineItems().size());
    assertSame(lineItem, request.getLineItems().get(0));
  }

  @Test
  public void settersReplaceValues() {
    CreateOrderRequest request = new CreateOrderRequest(1L, 2L, Collections.emptyList());
    CreateOrderRequest.LineItem lineItem = new CreateOrderRequest.LineItem("a", 1);

    request.setConsumerId(100L);
    request.setRestaurantId(200L);
    request.setLineItems(Collections.singletonList(lineItem));

    assertEquals(100L, request.getConsumerId());
    assertEquals(200L, request.getRestaurantId());
    assertEquals(Collections.singletonList(lineItem), request.getLineItems());
  }

  @Test
  public void lineItemConstructorAndSetters() {
    CreateOrderRequest.LineItem lineItem = new CreateOrderRequest.LineItem("chicken-vindaloo", 3);
    assertEquals("chicken-vindaloo", lineItem.getMenuItemId());
    assertEquals(3, lineItem.getQuantity());

    lineItem.setMenuItemId("naan");
    lineItem.setQuantity(5);

    assertEquals("naan", lineItem.getMenuItemId());
    assertEquals(5, lineItem.getQuantity());
  }

  @Test
  public void deserializesFromJsonUsingPrivateNoArgConstructors() throws Exception {
    String json = "{\"consumerId\":7,\"restaurantId\":8,"
            + "\"lineItems\":[{\"menuItemId\":\"m1\",\"quantity\":2},{\"menuItemId\":\"m2\",\"quantity\":4}]}";

    CreateOrderRequest request = objectMapper.readValue(json, CreateOrderRequest.class);

    assertEquals(7L, request.getConsumerId());
    assertEquals(8L, request.getRestaurantId());
    assertEquals(2, request.getLineItems().size());
    assertEquals("m1", request.getLineItems().get(0).getMenuItemId());
    assertEquals(2, request.getLineItems().get(0).getQuantity());
    assertEquals("m2", request.getLineItems().get(1).getMenuItemId());
    assertEquals(4, request.getLineItems().get(1).getQuantity());
  }

  @Test
  public void deserializingEmptyObjectLeavesDefaults() throws Exception {
    CreateOrderRequest request = objectMapper.readValue("{}", CreateOrderRequest.class);

    assertEquals(0L, request.getConsumerId());
    assertEquals(0L, request.getRestaurantId());
    assertNull(request.getLineItems());
  }

  @Test
  public void serializesAllFields() throws Exception {
    CreateOrderRequest request = new CreateOrderRequest(3L, 4L,
            Arrays.asList(new CreateOrderRequest.LineItem("x", 1), new CreateOrderRequest.LineItem("y", 9)));

    JsonNode node = objectMapper.readTree(objectMapper.writeValueAsString(request));

    assertEquals(3L, node.get("consumerId").asLong());
    assertEquals(4L, node.get("restaurantId").asLong());
    assertTrue(node.get("lineItems").isArray());
    assertEquals(2, node.get("lineItems").size());
    assertEquals("y", node.get("lineItems").get(1).get("menuItemId").asText());
    assertEquals(9, node.get("lineItems").get(1).get("quantity").asInt());
  }
}
