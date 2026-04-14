package net.chrisrichardson.ftgo.domain;

import net.chrisrichardson.ftgo.common.Money;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.Assert.*;

public class OrderLineItemsTest {

  private OrderLineItems orderLineItems;

  @Before
  public void setUp() {
    orderLineItems = new OrderLineItems(Arrays.asList(
        new OrderLineItem("item1", "Chicken", new Money("10.00"), 2),
        new OrderLineItem("item2", "Rice", new Money("5.00"), 3)
    ));
  }

  @Test
  public void shouldCalculateOrderTotal() {
    // 10*2 + 5*3 = 35
    assertEquals(new Money("35.00"), orderLineItems.orderTotal());
  }

  @Test
  public void shouldFindOrderLineItem() {
    OrderLineItem found = orderLineItems.findOrderLineItem("item1");
    assertEquals("Chicken", found.getName());
  }

  @Test
  public void shouldCalculateChangeToOrderTotal() {
    Map<String, Integer> revisedQuantities = new HashMap<>();
    revisedQuantities.put("item1", 5); // delta = 10 * (5-2) = 30
    OrderRevision revision = new OrderRevision(Optional.empty(), revisedQuantities);

    Money change = orderLineItems.changeToOrderTotal(revision);
    assertEquals(new Money("30.00"), change);
  }

  @Test
  public void shouldCalculateLineItemQuantityChange() {
    Map<String, Integer> revisedQuantities = new HashMap<>();
    revisedQuantities.put("item1", 5); // delta = 10 * (5-2) = 30
    OrderRevision revision = new OrderRevision(Optional.empty(), revisedQuantities);

    LineItemQuantityChange change = orderLineItems.lineItemQuantityChange(revision);
    assertEquals(new Money("35.00"), change.getCurrentOrderTotal());
    assertEquals(new Money("30.00"), change.getDelta());
    assertEquals(new Money("65.00"), change.getNewOrderTotal());
  }

  @Test
  public void shouldUpdateLineItems() {
    Map<String, Integer> revisedQuantities = new HashMap<>();
    revisedQuantities.put("item1", 10);
    revisedQuantities.put("item2", 1);
    OrderRevision revision = new OrderRevision(Optional.empty(), revisedQuantities);

    orderLineItems.updateLineItems(revision);

    assertEquals(10, orderLineItems.getLineItems().get(0).getQuantity());
    assertEquals(1, orderLineItems.getLineItems().get(1).getQuantity());
  }

  @Test
  public void shouldGetAndSetLineItems() {
    OrderLineItems items = new OrderLineItems(Collections.emptyList());
    assertTrue(items.getLineItems().isEmpty());

    items.setLineItems(Arrays.asList(new OrderLineItem("x", "Pizza", new Money("8.00"), 1)));
    assertEquals(1, items.getLineItems().size());
  }
}
