package net.chrisrichardson.ftgo.domain;

import org.junit.Before;
import org.junit.Test;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.junit.Assert.*;

public class ActionTest {

  private Order order;

  @Before
  public void setUp() {
    Restaurant restaurant = new Restaurant(1L, "Test",
        new RestaurantMenu(Collections.emptyList()));
    order = new Order(1L, restaurant, Collections.emptyList());
    order.setId(1L);
  }

  @Test
  public void shouldMakePickupAction() {
    Action pickup = Action.makePickup(order);
    assertEquals(ActionType.PICKUP, pickup.getType());
  }

  @Test
  public void shouldMakeDropoffAction() {
    LocalDateTime deliveryTime = LocalDateTime.now().plusHours(1);
    Action dropoff = Action.makeDropoff(order, deliveryTime);
    assertEquals(ActionType.DROPOFF, dropoff.getType());
  }

  @Test
  public void shouldMatchActionForSameOrder() {
    Action pickup = Action.makePickup(order);
    assertTrue(pickup.actionFor(order));
  }

  @Test
  public void shouldNotMatchActionForDifferentOrder() {
    Action pickup = Action.makePickup(order);
    Restaurant restaurant = new Restaurant(2L, "Other",
        new RestaurantMenu(Collections.emptyList()));
    Order otherOrder = new Order(2L, restaurant, Collections.emptyList());
    otherOrder.setId(2L);
    assertFalse(pickup.actionFor(otherOrder));
  }
}
