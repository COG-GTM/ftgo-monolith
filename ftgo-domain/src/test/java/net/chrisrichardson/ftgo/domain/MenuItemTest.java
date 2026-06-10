package net.chrisrichardson.ftgo.domain;

import net.chrisrichardson.ftgo.common.Money;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MenuItemTest {

  @Test
  void shouldCreateMenuItem() {
    MenuItem item = new MenuItem("1", "Chicken", new Money("10.00"));

    assertThat(item.getId()).isEqualTo("1");
    assertThat(item.getName()).isEqualTo("Chicken");
    assertThat(item.getPrice()).isEqualTo(new Money("10.00"));
  }

  @Test
  void shouldSupportSetters() {
    MenuItem item = new MenuItem("1", "Chicken", new Money("10.00"));
    item.setId("2");
    item.setName("Rice");
    item.setPrice(new Money("5.00"));

    assertThat(item.getId()).isEqualTo("2");
    assertThat(item.getName()).isEqualTo("Rice");
    assertThat(item.getPrice()).isEqualTo(new Money("5.00"));
  }

  @Test
  void shouldHaveEqualsAndHashCode() {
    MenuItem item1 = new MenuItem("1", "Chicken", new Money("10.00"));
    MenuItem item2 = new MenuItem("1", "Chicken", new Money("10.00"));
    MenuItem item3 = new MenuItem("2", "Rice", new Money("5.00"));

    assertThat(item1).isEqualTo(item2);
    assertThat(item1.hashCode()).isEqualTo(item2.hashCode());
    assertThat(item1).isNotEqualTo(item3);
  }

  @Test
  void shouldHaveToString() {
    MenuItem item = new MenuItem("1", "Chicken", new Money("10.00"));
    assertThat(item.toString()).isNotEmpty();
  }
}
