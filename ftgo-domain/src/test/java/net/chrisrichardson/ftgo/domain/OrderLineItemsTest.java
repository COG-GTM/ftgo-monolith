package net.chrisrichardson.ftgo.domain;

import net.chrisrichardson.ftgo.common.Money;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.Assert.assertEquals;

public class OrderLineItemsTest {

  private OrderLineItems lineItems;

  @Before
  public void setUp() {
    lineItems = new OrderLineItems(Arrays.asList(
            new OrderLineItem("item-1", "Chicken Vindaloo", new Money(10), 2),
            new OrderLineItem("item-2", "Garlic Naan", new Money(3), 4)));
  }

  @Test
  public void shouldComputeOrderTotal() {
    assertEquals(new Money(32), lineItems.orderTotal());
  }

  @Test
  public void shouldFindLineItemById() {
    assertEquals("Garlic Naan", lineItems.findOrderLineItem("item-2").getName());
  }

  @Test
  public void shouldComputeChangeToOrderTotal() {
    Map<String, Integer> revisedQuantities = Collections.singletonMap("item-1", 5);
    OrderRevision revision = new OrderRevision(Optional.empty(), revisedQuantities);

    assertEquals(new Money(30), lineItems.changeToOrderTotal(revision));
  }

  @Test
  public void shouldComputeLineItemQuantityChange() {
    Map<String, Integer> revisedQuantities = new HashMap<>();
    revisedQuantities.put("item-1", 1);
    revisedQuantities.put("item-2", 4);
    OrderRevision revision = new OrderRevision(Optional.empty(), revisedQuantities);

    LineItemQuantityChange change = lineItems.lineItemQuantityChange(revision);

    assertEquals(new Money(32), change.getCurrentOrderTotal());
    assertEquals(new Money(22), change.getNewOrderTotal());
    assertEquals(new Money(-10), change.getDelta());
  }

  @Test
  public void shouldUpdateOnlyRevisedLineItems() {
    OrderRevision revision =
            new OrderRevision(Optional.empty(), Collections.singletonMap("item-1", 5));

    lineItems.updateLineItems(revision);

    assertEquals(5, lineItems.findOrderLineItem("item-1").getQuantity());
    assertEquals(4, lineItems.findOrderLineItem("item-2").getQuantity());
    assertEquals(new Money(62), lineItems.orderTotal());
  }

  @Test
  public void shouldUpdateLineItemQuantities() {
    Map<String, Integer> revisedQuantities = new HashMap<>();
    revisedQuantities.put("item-1", 7);
    revisedQuantities.put("item-2", 1);
    OrderRevision revision = new OrderRevision(Optional.empty(), revisedQuantities);

    lineItems.updateLineItems(revision);

    assertEquals(7, lineItems.findOrderLineItem("item-1").getQuantity());
    assertEquals(1, lineItems.findOrderLineItem("item-2").getQuantity());
    assertEquals(new Money(73), lineItems.orderTotal());
  }
}
