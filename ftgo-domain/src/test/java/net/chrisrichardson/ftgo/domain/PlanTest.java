package net.chrisrichardson.ftgo.domain;

import net.chrisrichardson.ftgo.common.Address;
import net.chrisrichardson.ftgo.common.Money;
import org.junit.Before;
import org.junit.Test;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class PlanTest {

  private Plan plan;
  private Order order;

  @Before
  public void setUp() {
    plan = new Plan();
    Restaurant restaurant = new Restaurant("R", new Address("1", null, "C", "S", "Z"),
            new RestaurantMenu(Arrays.asList(new MenuItem("i1", "Item", new Money("10.00")))));
    List<OrderLineItem> items = Arrays.asList(new OrderLineItem("i1", "Item", new Money("10.00"), 1));
    order = new Order(1L, restaurant, items);
    order.setId(1L);
  }

  @Test
  public void shouldStartEmpty() {
    assertThat(plan.getActions()).isEmpty();
  }

  @Test
  public void shouldAddAction() {
    plan.add(Action.makePickup(order));
    assertThat(plan.getActions()).hasSize(1);
  }

  @Test
  public void shouldRemoveDeliveryActions() {
    plan.add(Action.makePickup(order));
    plan.add(Action.makeDropoff(order, LocalDateTime.now().plusHours(1)));
    assertThat(plan.getActions()).hasSize(2);

    plan.removeDelivery(order);
    assertThat(plan.getActions()).isEmpty();
  }

  @Test
  public void shouldNotRemoveActionsForDifferentOrder() {
    plan.add(Action.makePickup(order));

    Restaurant restaurant2 = new Restaurant("R2", new Address("2", null, "C", "S", "Z"),
            new RestaurantMenu(Arrays.asList(new MenuItem("i2", "Item2", new Money("5.00")))));
    List<OrderLineItem> items2 = Arrays.asList(new OrderLineItem("i2", "Item2", new Money("5.00"), 1));
    Order order2 = new Order(2L, restaurant2, items2);
    order2.setId(2L);

    plan.removeDelivery(order2);
    assertThat(plan.getActions()).hasSize(1);
  }

  @Test
  public void shouldGetActionsForDelivery() {
    plan.add(Action.makePickup(order));
    plan.add(Action.makeDropoff(order, LocalDateTime.now().plusHours(1)));

    List<Action> actions = plan.actionsForDelivery(order);
    assertThat(actions).hasSize(2);
  }
}
