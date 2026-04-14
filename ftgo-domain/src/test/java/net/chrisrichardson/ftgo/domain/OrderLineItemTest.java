package net.chrisrichardson.ftgo.domain;

import net.chrisrichardson.ftgo.common.Money;
import org.junit.Test;

import static org.junit.Assert.*;

public class OrderLineItemTest {

  @Test
  public void shouldCalculateTotal() {
    OrderLineItem item = new OrderLineItem("1", "Chicken", new Money("10.00"), 3);
    assertEquals(new Money("30.00"), item.getTotal());
  }

  @Test
  public void shouldCalculateDeltaForChangedQuantity() {
    OrderLineItem item = new OrderLineItem("1", "Chicken", new Money("10.00"), 2);
    // new quantity 5, delta = price * (5 - 2) = 10 * 3 = 30
    assertEquals(new Money("30.00"), item.deltaForChangedQuantity(5));
  }

  @Test
  public void shouldCalculateNegativeDeltaForReducedQuantity() {
    OrderLineItem item = new OrderLineItem("1", "Chicken", new Money("10.00"), 5);
    // new quantity 2, delta = price * (2 - 5) = 10 * -3 = -30
    assertEquals(new Money("-30.00"), item.deltaForChangedQuantity(2));
  }

  @Test
  public void shouldReturnZeroDeltaForSameQuantity() {
    OrderLineItem item = new OrderLineItem("1", "Chicken", new Money("10.00"), 3);
    assertEquals(new Money("0.00"), item.deltaForChangedQuantity(3));
  }

  @Test
  public void shouldGetAndSetProperties() {
    OrderLineItem item = new OrderLineItem();
    item.setMenuItemId("item1");
    item.setName("Pizza");
    item.setPrice(new Money("15.00"));
    item.setQuantity(2);

    assertEquals("item1", item.getMenuItemId());
    assertEquals("Pizza", item.getName());
    assertEquals(new Money("15.00"), item.getPrice());
    assertEquals(2, item.getQuantity());
  }

  @Test
  public void shouldHaveEqualityBasedOnFields() {
    OrderLineItem item1 = new OrderLineItem("1", "Chicken", new Money("10.00"), 2);
    OrderLineItem item2 = new OrderLineItem("1", "Chicken", new Money("10.00"), 2);
    assertEquals(item1, item2);
    assertEquals(item1.hashCode(), item2.hashCode());
  }

  @Test
  public void shouldNotBeEqualForDifferentFields() {
    OrderLineItem item1 = new OrderLineItem("1", "Chicken", new Money("10.00"), 2);
    OrderLineItem item2 = new OrderLineItem("2", "Beef", new Money("15.00"), 3);
    assertNotEquals(item1, item2);
  }

  @Test
  public void shouldHaveToString() {
    OrderLineItem item = new OrderLineItem("1", "Chicken", new Money("10.00"), 2);
    assertNotNull(item.toString());
  }
}
