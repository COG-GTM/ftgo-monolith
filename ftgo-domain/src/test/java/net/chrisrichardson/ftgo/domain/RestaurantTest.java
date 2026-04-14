package net.chrisrichardson.ftgo.domain;

import net.chrisrichardson.ftgo.common.Address;
import net.chrisrichardson.ftgo.common.Money;
import org.junit.Test;

import java.util.Arrays;
import java.util.Optional;

import static org.junit.Assert.*;

public class RestaurantTest {

  @Test
  public void shouldCreateWithNameAddressAndMenu() {
    Address address = new Address("1 Main St", "", "Springfield", "IL", "62701");
    RestaurantMenu menu = new RestaurantMenu(Arrays.asList(
        new MenuItem("1", "Burger", new Money("10.00"))));
    Restaurant restaurant = new Restaurant("Bob's Burgers", address, menu);

    assertEquals("Bob's Burgers", restaurant.getName());
    assertEquals(address, restaurant.getAddress());
  }

  @Test
  public void shouldCreateWithIdNameAndMenu() {
    RestaurantMenu menu = new RestaurantMenu(Arrays.asList(
        new MenuItem("1", "Burger", new Money("10.00"))));
    Restaurant restaurant = new Restaurant(42L, "Bob's Burgers", menu);

    assertEquals(Long.valueOf(42L), restaurant.getId());
    assertEquals("Bob's Burgers", restaurant.getName());
  }

  @Test
  public void shouldFindMenuItem() {
    RestaurantMenu menu = new RestaurantMenu(Arrays.asList(
        new MenuItem("1", "Burger", new Money("10.00")),
        new MenuItem("2", "Fries", new Money("5.00"))));
    Restaurant restaurant = new Restaurant(1L, "Bob's", menu);

    Optional<MenuItem> found = restaurant.findMenuItem("2");
    assertTrue(found.isPresent());
    assertEquals("Fries", found.get().getName());
  }

  @Test
  public void shouldReturnEmptyForUnknownMenuItem() {
    RestaurantMenu menu = new RestaurantMenu(Arrays.asList(
        new MenuItem("1", "Burger", new Money("10.00"))));
    Restaurant restaurant = new Restaurant(1L, "Bob's", menu);

    Optional<MenuItem> found = restaurant.findMenuItem("999");
    assertFalse(found.isPresent());
  }

  @Test(expected = UnsupportedOperationException.class)
  public void shouldThrowOnReviseMenu() {
    RestaurantMenu menu = new RestaurantMenu(Arrays.asList(
        new MenuItem("1", "Burger", new Money("10.00"))));
    Restaurant restaurant = new Restaurant(1L, "Bob's", menu);

    restaurant.reviseMenu(menu);
  }

  @Test
  public void shouldSetAndGetIdAndName() {
    Restaurant restaurant = new Restaurant();
    restaurant.setId(5L);
    restaurant.setName("New Name");
    assertEquals(Long.valueOf(5L), restaurant.getId());
    assertEquals("New Name", restaurant.getName());
  }
}
