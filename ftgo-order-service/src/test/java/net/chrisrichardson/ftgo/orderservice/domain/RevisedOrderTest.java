package net.chrisrichardson.ftgo.orderservice.domain;

import net.chrisrichardson.ftgo.common.Address;
import net.chrisrichardson.ftgo.common.Money;
import net.chrisrichardson.ftgo.domain.*;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class RevisedOrderTest {

  @Test
  public void shouldStoreOrderAndChange() {
    Restaurant restaurant = new Restaurant("R",
            new Address("1", null, "C", "S", "Z"),
            new RestaurantMenu(Arrays.asList(new MenuItem("m1", "Item", new Money("10.00")))));
    List<OrderLineItem> items = Arrays.asList(new OrderLineItem("m1", "Item", new Money("10.00"), 2));
    Order order = new Order(1L, restaurant, items);

    LineItemQuantityChange change = new LineItemQuantityChange(
            new Money("20.00"), new Money("30.00"), new Money("10.00"));

    RevisedOrder revisedOrder = new RevisedOrder(order, change);
    assertThat(revisedOrder.getOrder()).isEqualTo(order);
    assertThat(revisedOrder.getChange()).isEqualTo(change);
  }
}
