package net.chrisrichardson.ftgo.domain;

import net.chrisrichardson.ftgo.common.Money;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RestaurantMenuTest {

  @Test
  void shouldCreateMenuWithItems() {
    MenuItem item = new MenuItem("1", "Chicken", new Money("10.00"));
    RestaurantMenu menu = new RestaurantMenu(Collections.singletonList(item));

    assertThat(menu.getMenuItems()).hasSize(1);
    assertThat(menu.getMenuItems().get(0).getName()).isEqualTo("Chicken");
  }

  @Test
  void shouldSetMenuItems() {
    RestaurantMenu menu = new RestaurantMenu(Collections.emptyList());
    List<MenuItem> newItems = Arrays.asList(
            new MenuItem("1", "Chicken", new Money("10.00")),
            new MenuItem("2", "Rice", new Money("5.00")));
    menu.setMenuItems(newItems);

    assertThat(menu.getMenuItems()).hasSize(2);
  }

  @Test
  void shouldHaveEqualsAndHashCode() {
    MenuItem item = new MenuItem("1", "Chicken", new Money("10.00"));
    RestaurantMenu menu1 = new RestaurantMenu(Collections.singletonList(item));
    RestaurantMenu menu2 = new RestaurantMenu(Collections.singletonList(item));

    assertThat(menu1).isEqualTo(menu2);
    assertThat(menu1.hashCode()).isEqualTo(menu2.hashCode());
  }

  @Test
  void shouldHaveToString() {
    RestaurantMenu menu = new RestaurantMenu(Collections.emptyList());
    assertThat(menu.toString()).isNotEmpty();
  }
}
