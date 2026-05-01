package net.chrisrichardson.ftgo.domain;

import net.chrisrichardson.ftgo.common.Address;
import org.junit.Before;
import org.junit.Test;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

public class PlanTest {

  private Plan plan;
  private Order order;
  private Restaurant restaurant;

  @Before
  public void setUp() {
    plan = new Plan();
    restaurant = new Restaurant("Test",
            new Address("1 Main St", null, "Oakland", "CA", "94612"),
            new RestaurantMenu(Collections.emptyList()));
    order = new Order(1L, restaurant, Collections.emptyList());
    order.setId(100L);
  }

  @Test
  public void shouldAddAction() {
    Action pickup = Action.makePickup(order);
    plan.add(pickup);

    assertEquals(1, plan.getActions().size());
    assertEquals(ActionType.PICKUP, plan.getActions().get(0).getType());
  }

  @Test
  public void shouldRemoveDeliveryActions() {
    plan.add(Action.makePickup(order));
    plan.add(Action.makeDropoff(order, LocalDateTime.now().plusHours(1)));

    Order otherOrder = new Order(2L, restaurant, Collections.emptyList());
    otherOrder.setId(200L);
    plan.add(Action.makePickup(otherOrder));

    assertEquals(3, plan.getActions().size());

    plan.removeDelivery(order);

    assertEquals(1, plan.getActions().size());
    assertTrue(plan.getActions().get(0).actionFor(otherOrder));
  }

  @Test
  public void shouldFilterActionsForDelivery() {
    plan.add(Action.makePickup(order));
    plan.add(Action.makeDropoff(order, LocalDateTime.now().plusHours(1)));

    Order otherOrder = new Order(2L, restaurant, Collections.emptyList());
    otherOrder.setId(200L);
    plan.add(Action.makePickup(otherOrder));

    List<Action> orderActions = plan.actionsForDelivery(order);
    assertEquals(2, orderActions.size());
  }

  @Test
  public void shouldHandleEmptyPlan() {
    assertTrue(plan.getActions().isEmpty());
    List<Action> actions = plan.actionsForDelivery(order);
    assertTrue(actions.isEmpty());
  }
}
