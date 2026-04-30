package net.chrisrichardson.ftgo.domain;

import org.junit.Test;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

public class PlanTest {

  @Test
  public void shouldAddAction() {
    Plan plan = new Plan();
    Order order = makeTestOrder(1L);
    Action action = Action.makePickup(order);
    plan.add(action);
    assertEquals(1, plan.getActions().size());
  }

  @Test
  public void shouldRemoveDeliveryActions() {
    Plan plan = new Plan();
    Order order1 = makeTestOrder(1L);
    Order order2 = makeTestOrder(2L);

    plan.add(Action.makePickup(order1));
    plan.add(Action.makeDropoff(order1, LocalDateTime.now().plusMinutes(30)));
    plan.add(Action.makePickup(order2));

    plan.removeDelivery(order1);

    assertEquals(1, plan.getActions().size());
    assertEquals(ActionType.PICKUP, plan.getActions().get(0).getType());
  }

  @Test
  public void shouldFilterActionsForDelivery() {
    Plan plan = new Plan();
    Order order1 = makeTestOrder(1L);
    Order order2 = makeTestOrder(2L);

    plan.add(Action.makePickup(order1));
    plan.add(Action.makePickup(order2));
    plan.add(Action.makeDropoff(order1, LocalDateTime.now().plusMinutes(30)));

    List<Action> actions = plan.actionsForDelivery(order1);
    assertEquals(2, actions.size());
  }

  @Test
  public void shouldHandleEmptyPlan() {
    Plan plan = new Plan();
    assertTrue(plan.getActions().isEmpty());

    Order order = makeTestOrder(1L);
    assertTrue(plan.actionsForDelivery(order).isEmpty());
  }

  private Order makeTestOrder(long id) {
    RestaurantMenu menu = new RestaurantMenu(Collections.emptyList());
    Restaurant restaurant = new Restaurant(1L, "Test", menu);
    Order order = new Order(1L, restaurant, Collections.emptyList());
    order.setId(id);
    return order;
  }
}
