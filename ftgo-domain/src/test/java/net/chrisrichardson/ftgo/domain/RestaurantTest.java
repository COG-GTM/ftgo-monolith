package net.chrisrichardson.ftgo.domain;

import net.chrisrichardson.ftgo.common.Address;
import net.chrisrichardson.ftgo.common.Money;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RestaurantTest {

  @Test
  void shouldCreateRestaurantWithNameAndMenu() {
    MenuItem item = new MenuItem("1", "Chicken", new Money("10.00"));
    RestaurantMenu menu = new RestaurantMenu(Collections.singletonList(item));

    Restaurant restaurant = new Restaurant(1L, "Ajanta", menu);

    assertThat(restaurant.getId()).isEqualTo(1L);
    assertThat(restaurant.getName()).isEqualTo("Ajanta");
  }

  @Test
  void shouldCreateRestaurantWithAddress() {
    Address address = new Address("1 Main St", null, "Oakland", "CA", "94612");
    RestaurantMenu menu = new RestaurantMenu(Collections.emptyList());

    Restaurant restaurant = new Restaurant("Ajanta", address, menu);

    assertThat(restaurant.getName()).isEqualTo("Ajanta");
    assertThat(restaurant.getAddress()).isEqualTo(address);
  }

  @Test
  void shouldFindMenuItem() {
    MenuItem item1 = new MenuItem("1", "Chicken", new Money("10.00"));
    MenuItem item2 = new MenuItem("2", "Rice", new Money("5.00"));
    RestaurantMenu menu = new RestaurantMenu(Arrays.asList(item1, item2));

    Restaurant restaurant = new Restaurant(1L, "Test", menu);

    Optional<MenuItem> found = restaurant.findMenuItem("1");
    assertThat(found).isPresent();
    assertThat(found.get().getName()).isEqualTo("Chicken");
  }

  @Test
  void shouldReturnEmptyWhenMenuItemNotFound() {
    RestaurantMenu menu = new RestaurantMenu(Collections.singletonList(
            new MenuItem("1", "Chicken", new Money("10.00"))));
    Restaurant restaurant = new Restaurant(1L, "Test", menu);

    assertThat(restaurant.findMenuItem("999")).isEmpty();
  }

  @Test
  void shouldSetIdAndName() {
    Restaurant restaurant = new Restaurant();
    restaurant.setId(42L);
    restaurant.setName("Updated");

    assertThat(restaurant.getId()).isEqualTo(42L);
    assertThat(restaurant.getName()).isEqualTo("Updated");
  }

  @Test
  void shouldThrowOnReviseMenu() {
    Restaurant restaurant = new Restaurant(1L, "Test",
            new RestaurantMenu(Collections.emptyList()));

    assertThatThrownBy(() -> restaurant.reviseMenu(new RestaurantMenu(Collections.emptyList())))
            .isInstanceOf(UnsupportedOperationException.class);
  }
}
