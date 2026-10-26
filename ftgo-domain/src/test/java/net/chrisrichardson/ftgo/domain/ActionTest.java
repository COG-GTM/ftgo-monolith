package net.chrisrichardson.ftgo.domain;

import net.chrisrichardson.ftgo.common.Address;
import net.chrisrichardson.ftgo.common.Money;
import org.junit.Before;
import org.junit.Test;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class ActionTest {

  private Order order;

  @Before
  public void setUp() {
    Restaurant restaurant = new Restaurant("R", new Address("1", null, "C", "S", "Z"),
            new RestaurantMenu(Arrays.asList(new MenuItem("i1", "Item", new Money("10.00")))));
    List<OrderLineItem> items = Arrays.asList(new OrderLineItem("i1", "Item", new Money("10.00"), 1));
    order = new Order(1L, restaurant, items);
    order.setId(1L);
  }

  @Test
  public void shouldMakePickupAction() {
    Action action = Action.makePickup(order);
    assertThat(action.getType()).isEqualTo(ActionType.PICKUP);
    assertThat(action.getTime()).isNull();
  }

  @Test
  public void shouldMakeDropoffAction() {
    LocalDateTime deliveryTime = LocalDateTime.now().plusHours(1);
    Action action = Action.makeDropoff(order, deliveryTime);
    assertThat(action.getType()).isEqualTo(ActionType.DROPOFF);
    assertThat(action.getTime()).isEqualTo(deliveryTime);
  }

  @Test
  public void shouldMatchActionForSameOrder() {
    Action action = Action.makePickup(order);
    assertThat(action.actionFor(order)).isTrue();
  }

  @Test
  public void shouldNotMatchActionForDifferentOrder() {
    Action action = Action.makePickup(order);

    Restaurant restaurant2 = new Restaurant("R2", new Address("2", null, "C", "S", "Z"),
            new RestaurantMenu(Arrays.asList(new MenuItem("i2", "Item2", new Money("5.00")))));
    List<OrderLineItem> items2 = Arrays.asList(new OrderLineItem("i2", "Item2", new Money("5.00"), 1));
    Order order2 = new Order(2L, restaurant2, items2);
    order2.setId(2L);

    assertThat(action.actionFor(order2)).isFalse();
  }

  @Test
  public void shouldCreateWithConstructor() {
    LocalDateTime time = LocalDateTime.now();
    Action action = new Action(ActionType.PICKUP, order, time);
    assertThat(action.getType()).isEqualTo(ActionType.PICKUP);
    assertThat(action.getTime()).isEqualTo(time);
  }
}
