package net.chrisrichardson.ftgo.orderservice.domain;

import net.chrisrichardson.ftgo.common.Money;
import net.chrisrichardson.ftgo.domain.*;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

class RevisedOrderTest {

  @Test
  void shouldCreateRevisedOrder() {
    Restaurant restaurant = new Restaurant(1L, "Test",
            new RestaurantMenu(Collections.emptyList()));
    Order order = new Order(100L, restaurant, Collections.singletonList(
            new OrderLineItem("1", "Chicken", new Money("10.00"), 2)));

    Money currentTotal = new Money("20.00");
    Money newTotal = new Money("30.00");
    Money delta = new Money("10.00");
    LineItemQuantityChange change = new LineItemQuantityChange(currentTotal, newTotal, delta);

    RevisedOrder revisedOrder = new RevisedOrder(order, change);

    assertThat(revisedOrder.getOrder()).isEqualTo(order);
    assertThat(revisedOrder.getChange()).isEqualTo(change);
    assertThat(revisedOrder.getChange().getDelta()).isEqualTo(new Money("10.00"));
  }
}
