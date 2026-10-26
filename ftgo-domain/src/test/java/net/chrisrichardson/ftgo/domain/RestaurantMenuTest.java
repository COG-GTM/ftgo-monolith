package net.chrisrichardson.ftgo.domain;

import net.chrisrichardson.ftgo.common.Money;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class RestaurantMenuTest {

  @Test
  public void shouldCreateRestaurantMenu() {
    List<MenuItem> items = Arrays.asList(
            new MenuItem("m1", "Pizza", new Money("12.00")),
            new MenuItem("m2", "Pasta", new Money("14.00"))
    );
    RestaurantMenu menu = new RestaurantMenu(items);
    assertThat(menu.getMenuItems()).hasSize(2);
  }

  @Test
  public void shouldHaveEqualsAndHashCode() {
    List<MenuItem> items1 = Arrays.asList(new MenuItem("m1", "Pizza", new Money("12.00")));
    List<MenuItem> items2 = Arrays.asList(new MenuItem("m1", "Pizza", new Money("12.00")));
    RestaurantMenu menu1 = new RestaurantMenu(items1);
    RestaurantMenu menu2 = new RestaurantMenu(items2);
    assertThat(menu1).isEqualTo(menu2);
    assertThat(menu1.hashCode()).isEqualTo(menu2.hashCode());
  }

  @Test
  public void shouldHaveToString() {
    List<MenuItem> items = Arrays.asList(new MenuItem("m1", "Pizza", new Money("12.00")));
    RestaurantMenu menu = new RestaurantMenu(items);
    assertThat(menu.toString()).isNotEmpty();
  }

  @Test
  public void shouldSetMenuItems() {
    List<MenuItem> items = Arrays.asList(new MenuItem("m1", "Pizza", new Money("12.00")));
    RestaurantMenu menu = new RestaurantMenu(items);

    List<MenuItem> newItems = Arrays.asList(
            new MenuItem("m2", "Pasta", new Money("14.00")),
            new MenuItem("m3", "Salad", new Money("8.00"))
    );
    menu.setMenuItems(newItems);
    assertThat(menu.getMenuItems()).hasSize(2);
  }
}
