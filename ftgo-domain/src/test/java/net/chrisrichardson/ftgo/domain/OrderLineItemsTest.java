package net.chrisrichardson.ftgo.domain;

import net.chrisrichardson.ftgo.common.Money;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

public class OrderLineItemsTest {

  private OrderLineItems orderLineItems;

  @Before
  public void setUp() {
    List<OrderLineItem> items = Arrays.asList(
            new OrderLineItem("m1", "Burger", new Money("10.00"), 2),
            new OrderLineItem("m2", "Fries", new Money("5.00"), 1)
    );
    orderLineItems = new OrderLineItems(items);
  }

  @Test
  public void shouldCalculateOrderTotal() {
    assertThat(orderLineItems.orderTotal()).isEqualTo(new Money("25.00"));
  }

  @Test
  public void shouldGetLineItems() {
    assertThat(orderLineItems.getLineItems()).hasSize(2);
  }

  @Test
  public void shouldSetLineItems() {
    List<OrderLineItem> newItems = Arrays.asList(
            new OrderLineItem("m3", "Salad", new Money("8.00"), 1)
    );
    orderLineItems.setLineItems(newItems);
    assertThat(orderLineItems.getLineItems()).hasSize(1);
  }

  @Test
  public void shouldCalculateLineItemQuantityChange() {
    Map<String, Integer> revised = new HashMap<>();
    revised.put("m1", 3); // increase by 1 => +10
    OrderRevision revision = new OrderRevision(Optional.empty(), revised);

    LineItemQuantityChange change = orderLineItems.lineItemQuantityChange(revision);
    assertThat(change.getCurrentOrderTotal()).isEqualTo(new Money("25.00"));
    // delta = (3-2)*10 = 10
    assertThat(change.getDelta()).isEqualTo(new Money("10.00"));
    assertThat(change.getNewOrderTotal()).isEqualTo(new Money("35.00"));
  }

  @Test
  public void shouldUpdateLineItems() {
    Map<String, Integer> revised = new HashMap<>();
    revised.put("m1", 5);
    revised.put("m2", 3);
    OrderRevision revision = new OrderRevision(Optional.empty(), revised);

    orderLineItems.updateLineItems(revision);

    assertThat(orderLineItems.getLineItems().get(0).getQuantity()).isEqualTo(5);
    assertThat(orderLineItems.getLineItems().get(1).getQuantity()).isEqualTo(3);
  }
}
