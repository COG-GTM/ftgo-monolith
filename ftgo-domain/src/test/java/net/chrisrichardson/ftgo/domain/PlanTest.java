package net.chrisrichardson.ftgo.domain;

import net.chrisrichardson.ftgo.common.Address;
import net.chrisrichardson.ftgo.common.Money;
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
  private Restaurant restaurant;

  @Before
  public void setUp() {
    plan = new Plan();

    restaurant = new Restaurant("Test Restaurant",
            new Address("1 Main St", null, "Oakland", "CA", "94612"),
            new RestaurantMenu(Collections.singletonList(new MenuItem("1", "Chicken", new Money("12.00")))));

    order1 = new Order(1L, restaurant,
            Collections.singletonList(new OrderLineItem("1", "Chicken", new Money("12.00"), 1)));
    order1.setId(100L);

    order2 = new Order(2L, restaurant,
            Collections.singletonList(new OrderLineItem("1", "Chicken", new Money("12.00"), 1)));
    order2.setId(200L);
  }

  @Test
  public void shouldAddAction() {
    Action pickup = Action.makePickup(order1);
    plan.add(pickup);

    assertEquals(1, plan.getActions().size());
    assertSame(pickup, plan.getActions().get(0));
  }

  @Test
  public void shouldRemoveDeliveryActions() {
    plan.add(Action.makePickup(order1));
    plan.add(Action.makeDropoff(order1, LocalDateTime.now().plusHours(1)));
    plan.add(Action.makePickup(order2));

    assertEquals(3, plan.getActions().size());

    plan.removeDelivery(order1);

    assertEquals(1, plan.getActions().size());
    assertEquals(ActionType.PICKUP, plan.getActions().get(0).getType());
  }

  @Test
  public void shouldFilterActionsForDelivery() {
    Action pickup1 = Action.makePickup(order1);
    Action dropoff1 = Action.makeDropoff(order1, LocalDateTime.now().plusHours(1));
    Action pickup2 = Action.makePickup(order2);

    plan.add(pickup1);
    plan.add(dropoff1);
    plan.add(pickup2);

    List<Action> actionsForOrder1 = plan.actionsForDelivery(order1);

    assertEquals(2, actionsForOrder1.size());
    assertTrue(actionsForOrder1.contains(pickup1));
    assertTrue(actionsForOrder1.contains(dropoff1));
  }

  @Test
  public void shouldHandleEmptyPlan() {
    assertTrue(plan.getActions().isEmpty());
    assertEquals(0, plan.actionsForDelivery(order1).size());
  }
}
