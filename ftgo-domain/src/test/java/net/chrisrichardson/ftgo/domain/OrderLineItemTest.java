package net.chrisrichardson.ftgo.domain;

import net.chrisrichardson.ftgo.common.Money;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class OrderLineItemTest {

  @Test
  public void shouldCreateOrderLineItem() {
    OrderLineItem item = new OrderLineItem("m1", "Burger", new Money("10.00"), 2);
    assertThat(item.getMenuItemId()).isEqualTo("m1");
    assertThat(item.getName()).isEqualTo("Burger");
    assertThat(item.getPrice()).isEqualTo(new Money("10.00"));
    assertThat(item.getQuantity()).isEqualTo(2);
  }

  @Test
  public void shouldCalculateTotal() {
    OrderLineItem item = new OrderLineItem("m1", "Burger", new Money("10.00"), 3);
    assertThat(item.getTotal()).isEqualTo(new Money("30.00"));
  }

  @Test
  public void shouldCalculateDeltaForChangedQuantity() {
    OrderLineItem item = new OrderLineItem("m1", "Burger", new Money("10.00"), 2);
    Money delta = item.deltaForChangedQuantity(5);
    // (5 - 2) * 10 = 30
    assertThat(delta).isEqualTo(new Money("30.00"));
  }

  @Test
  public void shouldCalculateNegativeDelta() {
    OrderLineItem item = new OrderLineItem("m1", "Burger", new Money("10.00"), 3);
    Money delta = item.deltaForChangedQuantity(1);
    // (1 - 3) * 10 = -20
    assertThat(delta).isEqualTo(new Money("-20.00"));
  }

  @Test
  public void shouldSetQuantity() {
    OrderLineItem item = new OrderLineItem("m1", "Burger", new Money("10.00"), 2);
    item.setQuantity(5);
    assertThat(item.getQuantity()).isEqualTo(5);
  }

  @Test
  public void shouldHaveEqualsAndHashCode() {
    OrderLineItem item1 = new OrderLineItem("m1", "Burger", new Money("10.00"), 2);
    OrderLineItem item2 = new OrderLineItem("m1", "Burger", new Money("10.00"), 2);
    assertThat(item1).isEqualTo(item2);
    assertThat(item1.hashCode()).isEqualTo(item2.hashCode());
  }

  @Test
  public void shouldHaveToString() {
    OrderLineItem item = new OrderLineItem("m1", "Burger", new Money("10.00"), 2);
    assertThat(item.toString()).isNotEmpty();
  }

  @Test
  public void shouldCreateDefaultInstance() {
    OrderLineItem item = new OrderLineItem();
    assertThat(item.getMenuItemId()).isNull();
  }

  @Test
  public void shouldSetMenuItemId() {
    OrderLineItem item = new OrderLineItem("m1", "Burger", new Money("10.00"), 2);
    item.setMenuItemId("m2");
    assertThat(item.getMenuItemId()).isEqualTo("m2");
  }

  @Test
  public void shouldSetName() {
    OrderLineItem item = new OrderLineItem("m1", "Burger", new Money("10.00"), 2);
    item.setName("Pizza");
    assertThat(item.getName()).isEqualTo("Pizza");
  }

  @Test
  public void shouldSetPrice() {
    OrderLineItem item = new OrderLineItem("m1", "Burger", new Money("10.00"), 2);
    item.setPrice(new Money("15.00"));
    assertThat(item.getPrice()).isEqualTo(new Money("15.00"));
  }
}
