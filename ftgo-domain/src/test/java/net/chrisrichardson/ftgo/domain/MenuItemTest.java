package net.chrisrichardson.ftgo.domain;

import net.chrisrichardson.ftgo.common.Money;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class MenuItemTest {

  @Test
  public void shouldCreateMenuItem() {
    MenuItem item = new MenuItem("m1", "Pizza", new Money("12.00"));
    assertThat(item.getId()).isEqualTo("m1");
    assertThat(item.getName()).isEqualTo("Pizza");
    assertThat(item.getPrice()).isEqualTo(new Money("12.00"));
  }

  @Test
  public void shouldSetId() {
    MenuItem item = new MenuItem("m1", "Pizza", new Money("12.00"));
    item.setId("m2");
    assertThat(item.getId()).isEqualTo("m2");
  }

  @Test
  public void shouldSetName() {
    MenuItem item = new MenuItem("m1", "Pizza", new Money("12.00"));
    item.setName("Calzone");
    assertThat(item.getName()).isEqualTo("Calzone");
  }

  @Test
  public void shouldSetPrice() {
    MenuItem item = new MenuItem("m1", "Pizza", new Money("12.00"));
    item.setPrice(new Money("15.00"));
    assertThat(item.getPrice()).isEqualTo(new Money("15.00"));
  }

  @Test
  public void shouldHaveEqualsAndHashCode() {
    MenuItem item1 = new MenuItem("m1", "Pizza", new Money("12.00"));
    MenuItem item2 = new MenuItem("m1", "Pizza", new Money("12.00"));
    assertThat(item1).isEqualTo(item2);
    assertThat(item1.hashCode()).isEqualTo(item2.hashCode());
  }

  @Test
  public void shouldHaveToString() {
    MenuItem item = new MenuItem("m1", "Pizza", new Money("12.00"));
    assertThat(item.toString()).contains("Pizza");
  }
}
