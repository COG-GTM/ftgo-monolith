package net.chrisrichardson.ftgo.domain;

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
    MenuItem item1 = new MenuItem("item1", "Chicken Vindaloo", new Money("12.34"));
    MenuItem item2 = new MenuItem("item2", "Lamb Biryani", new Money("15.00"));
    RestaurantMenu menu = new RestaurantMenu(Arrays.asList(item1, item2));
    restaurant = new Restaurant(1L, "Ajanta", menu);
  }

  @Test
  public void shouldFindMenuItemById() {
    Optional<MenuItem> item = restaurant.findMenuItem("item1");
    assertTrue(item.isPresent());
    assertEquals("Chicken Vindaloo", item.get().getName());
  }

  @Test
  public void shouldReturnEmptyForUnknownMenuItem() {
    Optional<MenuItem> item = restaurant.findMenuItem("nonexistent");
    assertFalse(item.isPresent());
  }

  @Test(expected = UnsupportedOperationException.class)
  public void shouldThrowOnReviseMenu() {
    restaurant.reviseMenu(new RestaurantMenu(Arrays.asList()));
  }
}
