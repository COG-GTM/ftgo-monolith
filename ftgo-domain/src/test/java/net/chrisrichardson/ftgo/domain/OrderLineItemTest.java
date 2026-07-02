package net.chrisrichardson.ftgo.domain;

import net.chrisrichardson.ftgo.common.Money;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class OrderLineItemTest {

  @Test
  public void shouldComputeTotal() {
    OrderLineItem lineItem = new OrderLineItem("item-1", "Chicken Vindaloo", new Money("12.34"), 3);
    assertEquals(new Money("37.02"), lineItem.getTotal());
  }

  @Test
  public void shouldComputeDeltaForIncreasedQuantity() {
    OrderLineItem lineItem = new OrderLineItem("item-1", "Chicken Vindaloo", new Money(10), 2);
    assertEquals(new Money(30), lineItem.deltaForChangedQuantity(5));
  }

  @Test
  public void shouldComputeDeltaForDecreasedQuantity() {
    OrderLineItem lineItem = new OrderLineItem("item-1", "Chicken Vindaloo", new Money(10), 4);
    assertEquals(new Money(-20), lineItem.deltaForChangedQuantity(2));
  }

  @Test
  public void shouldExposeAccessors() {
    OrderLineItem lineItem = new OrderLineItem();
    lineItem.setMenuItemId("item-9");
    lineItem.setName("Samosa");
    lineItem.setPrice(new Money(4));
    lineItem.setQuantity(6);

    assertEquals("item-9", lineItem.getMenuItemId());
    assertEquals("Samosa", lineItem.getName());
    assertEquals(new Money(4), lineItem.getPrice());
    assertEquals(6, lineItem.getQuantity());
    assertEquals(new OrderLineItem("item-9", "Samosa", new Money(4), 6), lineItem);
  }
}
