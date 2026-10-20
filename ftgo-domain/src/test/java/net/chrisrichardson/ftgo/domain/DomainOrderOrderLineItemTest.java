package net.chrisrichardson.ftgo.domain;

import net.chrisrichardson.ftgo.common.Money;
import org.junit.Test;

import static net.chrisrichardson.ftgo.domain.DomainOrderFixtures.chickenVindaloo;
import static net.chrisrichardson.ftgo.domain.DomainOrderFixtures.money;
import static org.assertj.core.api.Assertions.assertThat;

public class DomainOrderOrderLineItemTest {

  @Test
  public void totalIsPriceTimesQuantity() {
    assertThat(chickenVindaloo(5).getTotal()).isEqualTo(money("61.70"));
  }

  @Test
  public void totalIsZeroForZeroQuantity() {
    assertThat(chickenVindaloo(0).getTotal()).isEqualTo(money("0.00"));
  }

  @Test
  public void deltaForIncreasedQuantityIsPositive() {
    assertThat(chickenVindaloo(5).deltaForChangedQuantity(7)).isEqualTo(money("24.68"));
  }

  @Test
  public void deltaForDecreasedQuantityIsNegative() {
    assertThat(chickenVindaloo(5).deltaForChangedQuantity(2)).isEqualTo(money("-37.02"));
  }

  @Test
  public void deltaForUnchangedQuantityIsZero() {
    assertThat(chickenVindaloo(5).deltaForChangedQuantity(5)).isEqualTo(money("0.00"));
  }

  @Test
  public void setQuantityChangesTotal() {
    OrderLineItem item = chickenVindaloo(1);
    item.setQuantity(3);
    assertThat(item.getTotal()).isEqualTo(money("37.02"));
  }

  @Test
  public void setPriceChangesTotal() {
    OrderLineItem item = chickenVindaloo(2);
    item.setPrice(new Money("1.25"));
    assertThat(item.getTotal()).isEqualTo(money("2.50"));
  }

  @Test
  public void itemsWithSameFieldsAreEqualWithSameHashCode() {
    OrderLineItem a = chickenVindaloo(5);
    OrderLineItem b = new OrderLineItem();
    b.setMenuItemId("1");
    b.setName("Chicken Vindaloo");
    b.setPrice(new Money("12.34"));
    b.setQuantity(5);

    assertThat(a).isEqualTo(b);
    assertThat(a.hashCode()).isEqualTo(b.hashCode());
  }

  @Test
  public void itemsWithDifferentQuantityAreNotEqual() {
    assertThat(chickenVindaloo(5)).isNotEqualTo(chickenVindaloo(6));
  }

  @Test
  public void toStringIncludesFieldValues() {
    assertThat(chickenVindaloo(5).toString())
            .contains("menuItemId=1")
            .contains("name=Chicken Vindaloo")
            .contains("quantity=5");
  }

  @Test
  public void gettersReturnConstructorValues() {
    OrderLineItem item = chickenVindaloo(5);

    assertThat(item.getMenuItemId()).isEqualTo("1");
    assertThat(item.getName()).isEqualTo("Chicken Vindaloo");
    assertThat(item.getPrice()).isEqualTo(money("12.34"));
    assertThat(item.getQuantity()).isEqualTo(5);
  }
}
