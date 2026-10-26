package net.chrisrichardson.ftgo.domain;

import net.chrisrichardson.ftgo.common.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrderLineItemsTest {

  private OrderLineItems orderLineItems;

  @BeforeEach
  void setUp() {
    List<OrderLineItem> items = Arrays.asList(
            new OrderLineItem("item1", "Chicken", new Money("10.00"), 2),
            new OrderLineItem("item2", "Rice", new Money("5.00"), 3)
    );
    orderLineItems = new OrderLineItems(items);
  }

  @Test
  void shouldCalculateOrderTotal() {
    Money total = orderLineItems.orderTotal();
    assertThat(total).isEqualTo(new Money("35.00"));
  }

  @Test
  void shouldGetLineItems() {
    assertThat(orderLineItems.getLineItems()).hasSize(2);
  }

  @Test
  void shouldSetLineItems() {
    List<OrderLineItem> newItems = Collections.singletonList(
            new OrderLineItem("item3", "Soup", new Money("7.00"), 1));
    orderLineItems.setLineItems(newItems);
    assertThat(orderLineItems.getLineItems()).hasSize(1);
  }

  @Test
  void shouldCalculateLineItemQuantityChange() {
    Map<String, Integer> revisedQuantities = new HashMap<>();
    revisedQuantities.put("item1", 4);
    OrderRevision revision = new OrderRevision(Optional.empty(), revisedQuantities);

    LineItemQuantityChange change = orderLineItems.lineItemQuantityChange(revision);

    assertThat(change.getCurrentOrderTotal()).isEqualTo(new Money("35.00"));
    assertThat(change.getDelta()).isEqualTo(new Money("20.00"));
    assertThat(change.getNewOrderTotal()).isEqualTo(new Money("55.00"));
  }

  @Test
  void shouldUpdateLineItems() {
    Map<String, Integer> revisedQuantities = new HashMap<>();
    revisedQuantities.put("item1", 5);
    revisedQuantities.put("item2", 1);
    OrderRevision revision = new OrderRevision(Optional.empty(), revisedQuantities);

    orderLineItems.updateLineItems(revision);

    assertThat(orderLineItems.getLineItems().get(0).getQuantity()).isEqualTo(5);
    assertThat(orderLineItems.getLineItems().get(1).getQuantity()).isEqualTo(1);
  }

  @Test
  void shouldFindOrderLineItem() {
    OrderLineItem found = orderLineItems.findOrderLineItem("item1");
    assertThat(found.getName()).isEqualTo("Chicken");
  }

  @Test
  void shouldThrowWhenLineItemNotFound() {
    assertThatThrownBy(() -> orderLineItems.findOrderLineItem("nonexistent"))
            .isInstanceOf(NoSuchElementException.class);
  }
}
