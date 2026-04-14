package net.chrisrichardson.ftgo.domain;

import org.junit.Before;
import org.junit.Test;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

public class PlanTest {

  private Plan plan;
  private Order order1;
  private Order order2;

  @Before
  public void setUp() {
    plan = new Plan();
    Restaurant restaurant = new Restaurant(1L, "Test",
        new RestaurantMenu(Collections.emptyList()));
    order1 = new Order(1L, restaurant, Collections.emptyList());
    order1.setId(1L);
    order2 = new Order(2L, restaurant, Collections.emptyList());
    order2.setId(2L);
  }

  @Test
  public void shouldStartEmpty() {
    assertTrue(plan.getActions().isEmpty());
  }

  @Test
  public void shouldAddAction() {
    Action pickup = Action.makePickup(order1);
    plan.add(pickup);
    assertEquals(1, plan.getActions().size());
  }

  @Test
  public void shouldAddMultipleActions() {
    plan.add(Action.makePickup(order1));
    plan.add(Action.makeDropoff(order1, LocalDateTime.now().plusHours(1)));
    plan.add(Action.makePickup(order2));
    assertEquals(3, plan.getActions().size());
  }

  @Test
  public void shouldRemoveDeliveryForOrder() {
    plan.add(Action.makePickup(order1));
    plan.add(Action.makeDropoff(order1, LocalDateTime.now().plusHours(1)));
    plan.add(Action.makePickup(order2));

    plan.removeDelivery(order1);
    assertEquals(1, plan.getActions().size());
  }

  @Test
  public void shouldReturnActionsForDelivery() {
    plan.add(Action.makePickup(order1));
    plan.add(Action.makeDropoff(order1, LocalDateTime.now().plusHours(1)));
    plan.add(Action.makePickup(order2));

    List<Action> actionsForOrder1 = plan.actionsForDelivery(order1);
    assertEquals(2, actionsForOrder1.size());
  }

  @Test
  public void shouldReturnEmptyActionsForUnknownOrder() {
    plan.add(Action.makePickup(order1));

    List<Action> actionsForOrder2 = plan.actionsForDelivery(order2);
    assertTrue(actionsForOrder2.isEmpty());
  }
}
