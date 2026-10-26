package net.chrisrichardson.ftgo.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

class ActionTest {

  @Test
  void shouldMakePickupAction() {
    Restaurant restaurant = new Restaurant(1L, "Test", new RestaurantMenu(Collections.emptyList()));
    Order order = new Order(1L, restaurant, Collections.emptyList());
    order.setId(10L);

    Action action = Action.makePickup(order);

    assertThat(action.getType()).isEqualTo(ActionType.PICKUP);
    assertThat(action.getTime()).isNull();
  }

  @Test
  void shouldMakeDropoffAction() {
    Restaurant restaurant = new Restaurant(1L, "Test", new RestaurantMenu(Collections.emptyList()));
    Order order = new Order(1L, restaurant, Collections.emptyList());
    order.setId(10L);
    LocalDateTime deliveryTime = LocalDateTime.now().plusMinutes(30);

    Action action = Action.makeDropoff(order, deliveryTime);

    assertThat(action.getType()).isEqualTo(ActionType.DROPOFF);
    assertThat(action.getTime()).isEqualTo(deliveryTime);
  }

  @Test
  void shouldCheckActionForOrder() {
    Restaurant restaurant = new Restaurant(1L, "Test", new RestaurantMenu(Collections.emptyList()));
    Order order = new Order(1L, restaurant, Collections.emptyList());
    order.setId(10L);

    Order otherOrder = new Order(2L, restaurant, Collections.emptyList());
    otherOrder.setId(20L);

    Action action = Action.makePickup(order);

    assertThat(action.actionFor(order)).isTrue();
    assertThat(action.actionFor(otherOrder)).isFalse();
  }
}
