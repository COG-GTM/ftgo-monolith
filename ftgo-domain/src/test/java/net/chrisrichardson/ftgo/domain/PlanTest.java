package net.chrisrichardson.ftgo.domain;

import org.junit.Before;
import org.junit.Test;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

public class PlanTest {

  private static final LocalDateTime DELIVERY_TIME = LocalDateTime.of(2026, 1, 1, 12, 30);

  private Plan plan;
  private Order order1;
  private Order order2;

  @Before
  public void setUp() {
    plan = new Plan();
    Restaurant restaurant = new Restaurant(1L, "Test Restaurant", new RestaurantMenu(Collections.emptyList()));
    order1 = makeOrder(restaurant, 101L);
    order2 = makeOrder(restaurant, 102L);
  }

  @Test
  public void shouldAddAction() {
    Action pickup = Action.makePickup(order1);
    Action dropoff = Action.makeDropoff(order1, DELIVERY_TIME);

    plan.add(pickup);
    plan.add(dropoff);

    assertEquals(Arrays.asList(pickup, dropoff), plan.getActions());
    assertEquals(ActionType.PICKUP, plan.getActions().get(0).getType());
    assertEquals(ActionType.DROPOFF, plan.getActions().get(1).getType());
    assertEquals(DELIVERY_TIME, plan.getActions().get(1).getTime());
  }

  @Test
  public void shouldRemoveDeliveryActions() {
    Action pickup1 = Action.makePickup(order1);
    Action dropoff1 = Action.makeDropoff(order1, DELIVERY_TIME);
    Action pickup2 = Action.makePickup(order2);
    Action dropoff2 = Action.makeDropoff(order2, DELIVERY_TIME);
    plan.add(pickup1);
    plan.add(pickup2);
    plan.add(dropoff1);
    plan.add(dropoff2);

    plan.removeDelivery(order1);

    assertEquals(Arrays.asList(pickup2, dropoff2), plan.getActions());
    assertTrue(plan.actionsForDelivery(order1).isEmpty());
  }

  @Test
  public void shouldMatchDeliveryByOrderIdNotInstance() {
    plan.add(Action.makePickup(order1));
    Order sameIdOrder = makeOrder(order1.getRestaurant(), order1.getId());

    plan.removeDelivery(sameIdOrder);

    assertTrue(plan.getActions().isEmpty());
  }

  @Test
  public void shouldFilterActionsForDelivery() {
    Action pickup1 = Action.makePickup(order1);
    Action dropoff1 = Action.makeDropoff(order1, DELIVERY_TIME);
    Action pickup2 = Action.makePickup(order2);
    plan.add(pickup1);
    plan.add(pickup2);
    plan.add(dropoff1);

    List<Action> actions = plan.actionsForDelivery(order1);

    assertEquals(Arrays.asList(pickup1, dropoff1), actions);
    assertEquals(3, plan.getActions().size());
  }

  @Test
  public void shouldHandleEmptyPlan() {
    assertTrue(plan.getActions().isEmpty());
    assertTrue(plan.actionsForDelivery(order1).isEmpty());

    plan.removeDelivery(order1);

    assertTrue(plan.getActions().isEmpty());
  }

  private Order makeOrder(Restaurant restaurant, Long id) {
    Order order = new Order(1L, restaurant, Collections.emptyList());
    order.setId(id);
    return order;
  }
}
