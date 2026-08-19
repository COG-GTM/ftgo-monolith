package net.chrisrichardson.ftgo.domain;

import net.chrisrichardson.ftgo.common.Address;
import net.chrisrichardson.ftgo.common.Money;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class OrderReviseTest {

  private static final String MENU_ITEM_ID = "1";

  private Order order;

  @Before
  public void setUp() {
    Restaurant restaurant = new Restaurant("Test Restaurant",
            new Address("1 Main St", null, "Oakland", "CA", "94612"),
            new RestaurantMenu(Collections.emptyList()));
    order = new Order(1L, restaurant,
            Collections.singletonList(new OrderLineItem(MENU_ITEM_ID, "Chicken Vindaloo", new Money("12.34"), 5)));
  }

  @Test
  public void shouldApplyValidRevision() {
    order.revise(revision(MENU_ITEM_ID, 10));

    assertEquals(new Money("123.40"), order.getOrderTotal());
  }

  @Test
  public void shouldRejectNegativeQuantity() {
    assertRejected(revision(MENU_ITEM_ID, -1000));
  }

  @Test
  public void shouldRejectZeroQuantity() {
    assertRejected(revision(MENU_ITEM_ID, 0));
  }

  @Test
  public void shouldRejectQuantityAboveMaximum() {
    assertRejected(revision(MENU_ITEM_ID, OrderLineItems.MAX_LINE_ITEM_QUANTITY + 1));
  }

  @Test
  public void shouldRejectNullQuantity() {
    assertRejected(revision(MENU_ITEM_ID, null));
  }

  @Test
  public void shouldRejectUnknownMenuItemId() {
    assertRejected(revision("unknown", 1));
  }

  @Test
  public void shouldRejectRevisionBelowOrderMinimum() {
    Restaurant restaurant = new Restaurant("Test Restaurant",
            new Address("1 Main St", null, "Oakland", "CA", "94612"),
            new RestaurantMenu(Collections.emptyList()));
    Order cheapOrder = new Order(1L, restaurant,
            Collections.singletonList(new OrderLineItem(MENU_ITEM_ID, "Samosa", new Money("1.00"), 20)));

    try {
      cheapOrder.revise(revision(MENU_ITEM_ID, 1));
      fail("Expected OrderMinimumNotMetException");
    } catch (OrderMinimumNotMetException expected) {
      assertEquals(new Money("20.00"), cheapOrder.getOrderTotal());
    }
  }

  private void assertRejected(OrderRevision revision) {
    Money totalBefore = order.getOrderTotal();
    try {
      order.revise(revision);
      fail("Expected InvalidOrderRevisionException");
    } catch (InvalidOrderRevisionException expected) {
      assertEquals(totalBefore, order.getOrderTotal());
    }
  }

  private OrderRevision revision(String menuItemId, Integer quantity) {
    Map<String, Integer> quantities = new HashMap<>();
    quantities.put(menuItemId, quantity);
    return new OrderRevision(Optional.empty(), quantities);
  }
}
