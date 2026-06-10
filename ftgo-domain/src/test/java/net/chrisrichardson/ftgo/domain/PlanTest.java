package net.chrisrichardson.ftgo.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PlanTest {

  private Plan plan;
  private Order order1;
  private Order order2;

  @BeforeEach
  void setUp() {
    plan = new Plan();
    Restaurant restaurant = new Restaurant(1L, "Test", new RestaurantMenu(Collections.emptyList()));
    order1 = new Order(1L, restaurant, Collections.emptyList());
    order1.setId(1L);
    order2 = new Order(2L, restaurant, Collections.emptyList());
    order2.setId(2L);
  }

  @Test
  void shouldStartEmpty() {
    assertThat(plan.getActions()).isEmpty();
  }

  @Test
  void shouldAddAction() {
    plan.add(Action.makePickup(order1));
    assertThat(plan.getActions()).hasSize(1);
  }

  @Test
  void shouldRemoveDeliveryForOrder() {
    plan.add(Action.makePickup(order1));
    plan.add(Action.makeDropoff(order1, LocalDateTime.now().plusMinutes(30)));
    plan.add(Action.makePickup(order2));

    plan.removeDelivery(order1);

    assertThat(plan.getActions()).hasSize(1);
    assertThat(plan.getActions().get(0).actionFor(order2)).isTrue();
  }

  @Test
  void shouldGetActionsForDelivery() {
    plan.add(Action.makePickup(order1));
    plan.add(Action.makePickup(order2));
    plan.add(Action.makeDropoff(order1, LocalDateTime.now().plusMinutes(30)));

    List<Action> actionsForOrder1 = plan.actionsForDelivery(order1);

    assertThat(actionsForOrder1).hasSize(2);
  }

  @Test
  void shouldReturnEmptyListWhenNoActionsForOrder() {
    plan.add(Action.makePickup(order1));

    List<Action> actions = plan.actionsForDelivery(order2);

    assertThat(actions).isEmpty();
  }
}
