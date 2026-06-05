package net.chrisrichardson.ftgo.domain;

import net.chrisrichardson.ftgo.common.Address;
import net.chrisrichardson.ftgo.common.Money;
import org.junit.Test;

import java.util.Arrays;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class RestaurantTest {

  @Test
  public void shouldCreateRestaurantWithNameAddressMenu() {
    RestaurantMenu menu = new RestaurantMenu(Arrays.asList(
            new MenuItem("m1", "Pasta", new Money("15.00"))
    ));
    Address address = new Address("100 Broadway", null, "NYC", "NY", "10001");
    Restaurant restaurant = new Restaurant("Italian Place", address, menu);

    assertThat(restaurant.getName()).isEqualTo("Italian Place");
    assertThat(restaurant.getAddress()).isEqualTo(address);
  }

  @Test
  public void shouldCreateRestaurantWithIdNameMenu() {
    RestaurantMenu menu = new RestaurantMenu(Arrays.asList(
            new MenuItem("m1", "Sushi", new Money("20.00"))
    ));
    Restaurant restaurant = new Restaurant(5L, "Sushi Bar", menu);

    assertThat(restaurant.getId()).isEqualTo(5L);
    assertThat(restaurant.getName()).isEqualTo("Sushi Bar");
  }

  @Test
  public void shouldFindMenuItem() {
    RestaurantMenu menu = new RestaurantMenu(Arrays.asList(
            new MenuItem("m1", "Burger", new Money("10.00")),
            new MenuItem("m2", "Fries", new Money("5.00"))
    ));
    Restaurant restaurant = new Restaurant("Grill", new Address("1", null, "C", "S", "Z"), menu);

    Optional<MenuItem> item = restaurant.findMenuItem("m2");
    assertThat(item).isPresent();
    assertThat(item.get().getName()).isEqualTo("Fries");
  }

  @Test
  public void shouldReturnEmptyForMissingMenuItem() {
    RestaurantMenu menu = new RestaurantMenu(Arrays.asList(
            new MenuItem("m1", "Burger", new Money("10.00"))
    ));
    Restaurant restaurant = new Restaurant("Grill", new Address("1", null, "C", "S", "Z"), menu);

    Optional<MenuItem> item = restaurant.findMenuItem("nonexistent");
    assertThat(item).isEmpty();
  }

  @Test
  public void shouldThrowOnReviseMenu() {
    RestaurantMenu menu = new RestaurantMenu(Arrays.asList(
            new MenuItem("m1", "Burger", new Money("10.00"))
    ));
    Restaurant restaurant = new Restaurant("Grill", new Address("1", null, "C", "S", "Z"), menu);

    assertThatThrownBy(() -> restaurant.reviseMenu(menu))
            .isInstanceOf(UnsupportedOperationException.class);
  }

  @Test
  public void shouldSetAndGetName() {
    Restaurant restaurant = new Restaurant();
    restaurant.setName("New Name");
    assertThat(restaurant.getName()).isEqualTo("New Name");
  }

  @Test
  public void shouldSetAndGetId() {
    Restaurant restaurant = new Restaurant();
    restaurant.setId(99L);
    assertThat(restaurant.getId()).isEqualTo(99L);
  }
}
