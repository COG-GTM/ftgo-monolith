package net.chrisrichardson.ftgo.domain;

import net.chrisrichardson.ftgo.common.Address;
import net.chrisrichardson.ftgo.common.Money;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Optional;

import static org.junit.Assert.*;

public class RestaurantTest {

  private Restaurant restaurant;

  @Before
  public void setUp() {
    MenuItem item1 = new MenuItem("1", "Chicken Vindaloo", new Money("12.34"));
    MenuItem item2 = new MenuItem("2", "Lamb Rogan Josh", new Money("15.99"));
    restaurant = new Restaurant("Ajanta",
            new Address("1 Main St", null, "Oakland", "CA", "94612"),
            new RestaurantMenu(Arrays.asList(item1, item2)));
  }

  @Test
  public void shouldFindMenuItemById() {
    Optional<MenuItem> item = restaurant.findMenuItem("1");
    assertTrue(item.isPresent());
    assertEquals("Chicken Vindaloo", item.get().getName());
  }

  @Test
  public void shouldReturnEmptyForUnknownMenuItem() {
    Optional<MenuItem> item = restaurant.findMenuItem("999");
    assertFalse(item.isPresent());
  }

  @Test(expected = UnsupportedOperationException.class)
  public void shouldThrowOnReviseMenu() {
    restaurant.reviseMenu(new RestaurantMenu(Arrays.asList(new MenuItem("3", "New Item", new Money("9.99")))));
  }
}
