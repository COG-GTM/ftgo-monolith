package net.chrisrichardson.ftgo.orderservice.api.events;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.chrisrichardson.ftgo.common.Money;
import net.chrisrichardson.ftgo.common.MoneyModule;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class OrderDetailsTest {

  private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new MoneyModule());

  private final List<OrderLineItemDTO> lineItems =
          Collections.singletonList(new OrderLineItemDTO(2, "1", "Chicken Vindaloo"));

  private OrderDetails details(long consumerId, long restaurantId, String total) {
    return new OrderDetails(consumerId, restaurantId, lineItems, new Money(total));
  }

  @Test
  public void constructorAssignsConsumerAndRestaurantToTheRightFields() {
    OrderDetails details = details(1L, 2L, "12.34");

    assertEquals(1L, details.getConsumerId());
    assertEquals(2L, details.getRestaurantId());
    assertSame(lineItems, details.getLineItems());
    assertEquals(new Money("12.34"), details.getOrderTotal());
  }

  @Test
  public void settersReplaceValues() {
    OrderDetails details = details(1L, 2L, "12.34");
    List<OrderLineItemDTO> newItems = Collections.singletonList(new OrderLineItemDTO(1, "2", "Naan"));

    details.setConsumerId(10L);
    details.setRestaurantId(20L);
    details.setLineItems(newItems);
    details.setOrderTotal(new Money("3.50"));

    assertEquals(10L, details.getConsumerId());
    assertEquals(20L, details.getRestaurantId());
    assertSame(newItems, details.getLineItems());
    assertEquals(new Money("3.50"), details.getOrderTotal());
  }

  @Test
  public void equalsAndHashCodeAreValueBasedWhenLineItemsAreShared() {
    OrderDetails a = details(1L, 2L, "12.34");
    OrderDetails b = details(1L, 2L, "12.34");

    assertEquals(a, b);
    assertEquals(a.hashCode(), b.hashCode());
    assertEquals(a, a);
  }

  @Test
  public void equalsDetectsDifferences() {
    OrderDetails base = details(1L, 2L, "12.34");

    assertNotEquals(base, details(9L, 2L, "12.34"));
    assertNotEquals(base, details(1L, 9L, "12.34"));
    assertNotEquals(base, details(1L, 2L, "99.99"));
    assertNotEquals(base, new OrderDetails(1L, 2L, Collections.<OrderLineItemDTO>emptyList(), new Money("12.34")));
    assertFalse(base.equals(null));
    assertFalse(base.equals("not order details"));
  }

  @Test
  public void equals_isFalseForValueEqualLineItems_becauseOrderLineItemDTOLacksEquals_bug() {
    OrderDetails a = new OrderDetails(1L, 2L,
            Collections.singletonList(new OrderLineItemDTO(2, "1", "Chicken Vindaloo")), new Money("12.34"));
    OrderDetails b = new OrderDetails(1L, 2L,
            Collections.singletonList(new OrderLineItemDTO(2, "1", "Chicken Vindaloo")), new Money("12.34"));

    assertNotEquals(a, b);
  }

  @Test
  public void toStringIncludesFieldValues() {
    String s = details(5L, 6L, "7.25").toString();

    assertThat(s, containsString("OrderDetails"));
    assertThat(s, containsString("consumerId=5"));
    assertThat(s, containsString("restaurantId=6"));
    assertThat(s, containsString("amount=7.25"));
    assertThat(s, containsString("lineItems="));
  }

  @Test
  public void serializesToJsonWithMoneyAsString() throws Exception {
    JsonNode node = objectMapper.readTree(objectMapper.writeValueAsString(details(1L, 2L, "12.34")));

    assertEquals(1L, node.get("consumerId").asLong());
    assertEquals(2L, node.get("restaurantId").asLong());
    assertTrue(node.get("orderTotal").isTextual());
    assertEquals("12.34", node.get("orderTotal").asText());
    assertEquals(1, node.get("lineItems").size());
    assertEquals("Chicken Vindaloo", node.get("lineItems").get(0).get("name").asText());
  }

  @Test
  public void deserializesFromJsonUsingPrivateNoArgConstructor() throws Exception {
    String json = "{\"consumerId\":3,\"restaurantId\":4,\"orderTotal\":\"8.00\","
            + "\"lineItems\":[{\"quantity\":1,\"menuItemId\":\"x\",\"name\":\"X\"}]}";

    OrderDetails details = objectMapper.readValue(json, OrderDetails.class);

    assertEquals(3L, details.getConsumerId());
    assertEquals(4L, details.getRestaurantId());
    assertEquals(new Money("8.00"), details.getOrderTotal());
    assertEquals(1, details.getLineItems().size());
    assertEquals("x", details.getLineItems().get(0).getMenuItemId());
    assertEquals("X", details.getLineItems().get(0).getName());
    assertEquals(1, details.getLineItems().get(0).getQuantity());
  }

  @Test
  public void deserializingEmptyObjectLeavesDefaults() throws Exception {
    OrderDetails details = objectMapper.readValue("{}", OrderDetails.class);

    assertEquals(0L, details.getConsumerId());
    assertEquals(0L, details.getRestaurantId());
    assertNull(details.getLineItems());
    assertNull(details.getOrderTotal());
  }

  @Test
  public void jsonRoundTripIsNotEqualToOriginal_becauseOrderLineItemDTOLacksEquals_bug() throws Exception {
    OrderDetails original = details(1L, 2L, "12.34");
    OrderDetails copy = objectMapper.readValue(objectMapper.writeValueAsString(original), OrderDetails.class);

    assertEquals(original.getOrderTotal(), copy.getOrderTotal());
    assertEquals(original.getConsumerId(), copy.getConsumerId());
    assertNotEquals(original, copy);
  }
}
