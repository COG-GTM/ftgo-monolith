package net.chrisrichardson.ftgo.domain;

import net.chrisrichardson.ftgo.common.Money;
import org.junit.Test;

import java.util.Collections;
import java.util.Optional;

import static org.junit.Assert.assertEquals;

public class OrderLineItemQuantityTest {

  private static final Money PRICE = new Money("12.34");

  @Test(expected = IllegalArgumentException.class)
  public void shouldRejectZeroQuantity() {
    new OrderLineItem("item-1", "Chicken Vindaloo", PRICE, 0);
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldRejectNegativeQuantity() {
    new OrderLineItem("item-1", "Chicken Vindaloo", PRICE, -5);
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldRejectNegativeRevisedQuantity() {
    OrderLineItems lineItems = new OrderLineItems(Collections.singletonList(
            new OrderLineItem("item-1", "Chicken Vindaloo", PRICE, 2)));
    lineItems.lineItemQuantityChange(new OrderRevision(Optional.empty(), Collections.singletonMap("item-1", -3)));
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldRejectRevisionOfUnknownLineItem() {
    OrderLineItems lineItems = new OrderLineItems(Collections.singletonList(
            new OrderLineItem("item-1", "Chicken Vindaloo", PRICE, 2)));
    lineItems.lineItemQuantityChange(new OrderRevision(Optional.empty(), Collections.singletonMap("nope", 1)));
  }

  @Test
  public void shouldComputePositiveTotalForValidRevision() {
    OrderLineItems lineItems = new OrderLineItems(Collections.singletonList(
            new OrderLineItem("item-1", "Chicken Vindaloo", PRICE, 2)));
    LineItemQuantityChange change = lineItems.lineItemQuantityChange(
            new OrderRevision(Optional.empty(), Collections.singletonMap("item-1", 3)));
    assertEquals(PRICE.multiply(3), change.newOrderTotal);
  }
}
