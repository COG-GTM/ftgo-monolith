package net.chrisrichardson.ftgo.domain;

import org.junit.Test;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.junit.Assert.*;

public class ActionTest {

  @Test
  public void makePickupCreatesPickupWithoutTime() {
    Action action = Action.makePickup(order(1L));

    assertEquals(ActionType.PICKUP, action.getType());
    assertNull(action.getTime());
  }

  @Test
  public void makeDropoffCreatesDropoffWithGivenTime() {
    LocalDateTime deliveryTime = LocalDateTime.of(2024, 5, 1, 12, 30);

    Action action = Action.makeDropoff(order(1L), deliveryTime);

    assertEquals(ActionType.DROPOFF, action.getType());
    assertEquals(deliveryTime, action.getTime());
  }

  @Test
  public void actionForMatchesDifferentOrderInstancesWithSameId() {
    Action action = Action.makePickup(order(10L));

    assertTrue(action.actionFor(order(10L)));
    assertFalse(action.actionFor(order(11L)));
  }

  @Test(expected = NullPointerException.class)
  public void actionForCurrentlyThrowsNpeWhenOrderIdIsNull() {
    // Suspected bug: unsaved orders have null ids.
    Action action = Action.makePickup(order(null));

    action.actionFor(order(1L));
  }

  private static Order order(Long id) {
    Order order = new Order(1L, null, Collections.emptyList());
    order.setId(id);
    return order;
  }
}
