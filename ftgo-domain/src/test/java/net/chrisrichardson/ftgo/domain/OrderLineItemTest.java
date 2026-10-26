package net.chrisrichardson.ftgo.domain;

import net.chrisrichardson.ftgo.common.Money;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OrderLineItemTest {

  @Test
  void shouldCreateOrderLineItem() {
    OrderLineItem item = new OrderLineItem("item1", "Chicken", new Money("10.00"), 3);

    assertThat(item.getMenuItemId()).isEqualTo("item1");
    assertThat(item.getName()).isEqualTo("Chicken");
    assertThat(item.getPrice()).isEqualTo(new Money("10.00"));
    assertThat(item.getQuantity()).isEqualTo(3);
  }

  @Test
  void shouldCalculateTotal() {
    OrderLineItem item = new OrderLineItem("item1", "Chicken", new Money("10.00"), 3);

    assertThat(item.getTotal()).isEqualTo(new Money("30.00"));
  }

  @Test
  void shouldCalculateDeltaForChangedQuantity() {
    OrderLineItem item = new OrderLineItem("item1", "Chicken", new Money("10.00"), 3);

    Money delta = item.deltaForChangedQuantity(5);
    assertThat(delta).isEqualTo(new Money("20.00"));
  }

  @Test
  void shouldCalculateNegativeDeltaForDecreasedQuantity() {
    OrderLineItem item = new OrderLineItem("item1", "Chicken", new Money("10.00"), 5);

    Money delta = item.deltaForChangedQuantity(2);
    assertThat(delta).isEqualTo(new Money("-30.00"));
  }

  @Test
  void shouldSupportSetters() {
    OrderLineItem item = new OrderLineItem();
    item.setMenuItemId("item2");
    item.setName("Pasta");
    item.setPrice(new Money("15.00"));
    item.setQuantity(2);

    assertThat(item.getMenuItemId()).isEqualTo("item2");
    assertThat(item.getName()).isEqualTo("Pasta");
    assertThat(item.getPrice()).isEqualTo(new Money("15.00"));
    assertThat(item.getQuantity()).isEqualTo(2);
  }

  @Test
  void shouldHaveToString() {
    OrderLineItem item = new OrderLineItem("item1", "Chicken", new Money("10.00"), 3);
    assertThat(item.toString()).isNotEmpty();
  }

  @Test
  void shouldHaveEqualsAndHashCode() {
    OrderLineItem item1 = new OrderLineItem("item1", "Chicken", new Money("10.00"), 3);
    OrderLineItem item2 = new OrderLineItem("item1", "Chicken", new Money("10.00"), 3);
    OrderLineItem item3 = new OrderLineItem("item2", "Pasta", new Money("15.00"), 2);

    assertThat(item1).isEqualTo(item2);
    assertThat(item1.hashCode()).isEqualTo(item2.hashCode());
    assertThat(item1).isNotEqualTo(item3);
  }
}
