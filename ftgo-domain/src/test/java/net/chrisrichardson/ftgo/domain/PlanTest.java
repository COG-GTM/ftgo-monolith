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
  private Order order1;
  private Order order2;

  @Before
  public void setUp() {
    plan = new Plan();
    Restaurant restaurant = new Restaurant("Ajanta", new Address("1 Main St", null, "Oakland", "CA", "94612"),
            new RestaurantMenu(Collections.emptyList()));
    order1 = makeOrder(restaurant, 1L);
    order2 = makeOrder(restaurant, 2L);
  }

  @Test
  public void shouldAddAction() {
    Action pickup = Action.makePickup(order1);

    plan.add(pickup);

    assertEquals(Collections.singletonList(pickup), plan.getActions());
  }

  @Test
  public void shouldRemoveDeliveryActions() {
    Action order2Pickup = Action.makePickup(order2);
    plan.add(Action.makePickup(order1));
    plan.add(order2Pickup);
    plan.add(Action.makeDropoff(order1, LocalDateTime.now().plusHours(1)));

    plan.removeDelivery(order1);

    assertEquals(Collections.singletonList(order2Pickup), plan.getActions());
  }

  @Test
  public void shouldFilterActionsForDelivery() {
    LocalDateTime deliveryTime = LocalDateTime.now().plusHours(1);
    plan.add(Action.makePickup(order1));
    plan.add(Action.makePickup(order2));
    plan.add(Action.makeDropoff(order1, deliveryTime));

    List<Action> actions = plan.actionsForDelivery(order1);

    assertEquals(2, actions.size());
    assertEquals(ActionType.PICKUP, actions.get(0).getType());
    assertNull(actions.get(0).getTime());
    assertEquals(ActionType.DROPOFF, actions.get(1).getType());
    assertEquals(deliveryTime, actions.get(1).getTime());
    assertEquals(3, plan.getActions().size());
  }

  @Test
  public void shouldHandleEmptyPlan() {
    assertTrue(plan.getActions().isEmpty());
    assertTrue(plan.actionsForDelivery(order1).isEmpty());

    plan.removeDelivery(order1);

    assertTrue(plan.getActions().isEmpty());
  }

  private Order makeOrder(Restaurant restaurant, long id) {
    Order order = new Order(1L, restaurant, Collections.emptyList());
    order.setId(id);
    return order;
  }
}
