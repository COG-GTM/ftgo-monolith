package net.chrisrichardson.ftgo.domain;

import net.chrisrichardson.ftgo.common.Address;
import net.chrisrichardson.ftgo.common.Money;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.junit.Assert.*;

public class RestaurantTest {

  @Test
  public void shouldCreateRestaurantWithNameAddressAndMenu() {
    Address address = new Address("1 Main St", null, "Oakland", "CA", "94612");
    MenuItem item = new MenuItem("1", "Chicken Vindaloo", new Money("12.34"));
    RestaurantMenu menu = new RestaurantMenu(Collections.singletonList(item));

    Restaurant restaurant = new Restaurant("Ajanta", address, menu);

    assertEquals("Ajanta", restaurant.getName());
    assertEquals(address, restaurant.getAddress());
  }

  @Test
  public void shouldCreateRestaurantWithIdNameAndMenu() {
    MenuItem item = new MenuItem("1", "Chicken Vindaloo", new Money("12.34"));
    RestaurantMenu menu = new RestaurantMenu(Collections.singletonList(item));

    Restaurant restaurant = new Restaurant(1L, "Ajanta", menu);

    assertEquals(Long.valueOf(1L), restaurant.getId());
    assertEquals("Ajanta", restaurant.getName());
  }

  @Test
  public void shouldFindMenuItemById() {
    MenuItem item1 = new MenuItem("1", "Chicken Vindaloo", new Money("12.34"));
    MenuItem item2 = new MenuItem("2", "Lamb Rogan Josh", new Money("15.99"));
    RestaurantMenu menu = new RestaurantMenu(Arrays.asList(item1, item2));

    Restaurant restaurant = new Restaurant("Ajanta",
            new Address("1 Main St", null, "Oakland", "CA", "94612"), menu);

    Optional<MenuItem> found = restaurant.findMenuItem("1");
    assertTrue(found.isPresent());
    assertEquals("Chicken Vindaloo", found.get().getName());
    assertEquals(new Money("12.34"), found.get().getPrice());
  }

  @Test
  public void shouldReturnEmptyForNonExistentMenuItem() {
    MenuItem item = new MenuItem("1", "Chicken Vindaloo", new Money("12.34"));
    RestaurantMenu menu = new RestaurantMenu(Collections.singletonList(item));

    Restaurant restaurant = new Restaurant("Ajanta",
            new Address("1 Main St", null, "Oakland", "CA", "94612"), menu);

    Optional<MenuItem> found = restaurant.findMenuItem("999");
    assertFalse(found.isPresent());
  }

  @Test
  public void shouldSetAndGetId() {
    Restaurant restaurant = new Restaurant("Test",
            new Address("1 Main St", null, "Oakland", "CA", "94612"),
            new RestaurantMenu(Collections.emptyList()));
    restaurant.setId(42L);
    assertEquals(Long.valueOf(42L), restaurant.getId());
  }

  @Test
  public void shouldSetAndGetName() {
    Restaurant restaurant = new Restaurant("Original",
            new Address("1 Main St", null, "Oakland", "CA", "94612"),
            new RestaurantMenu(Collections.emptyList()));
    restaurant.setName("Updated");
    assertEquals("Updated", restaurant.getName());
  }

  @Test(expected = UnsupportedOperationException.class)
  public void shouldThrowOnReviseMenu() {
    Restaurant restaurant = new Restaurant("Test",
            new Address("1 Main St", null, "Oakland", "CA", "94612"),
            new RestaurantMenu(Collections.emptyList()));
    restaurant.reviseMenu(new RestaurantMenu(Collections.emptyList()));
  }
}
