package net.chrisrichardson.ftgo.domain;

import net.chrisrichardson.ftgo.common.Money;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.junit.Assert.*;

public class RestaurantTest {

  private MenuItem chickenVindaloo;
  private MenuItem garlicNaan;
  private Restaurant restaurant;

  @Before
  public void setUp() {
    chickenVindaloo = new MenuItem("1", "Chicken Vindaloo", new Money("12.34"));
    garlicNaan = new MenuItem("2", "Garlic Naan", new Money("3.50"));
    restaurant = new Restaurant(1L, "Ajanta", new RestaurantMenu(Arrays.asList(chickenVindaloo, garlicNaan)));
  }

  @Test
  public void shouldFindMenuItemById() {
    Optional<MenuItem> found = restaurant.findMenuItem("2");

    assertTrue(found.isPresent());
    assertSame(garlicNaan, found.get());
    assertEquals("Garlic Naan", found.get().getName());
    assertEquals(new Money("3.50"), found.get().getPrice());
  }

  @Test
  public void shouldReturnEmptyForUnknownMenuItem() {
    assertFalse(restaurant.findMenuItem("999").isPresent());
  }

  @Test
  public void shouldReturnEmptyWhenMenuIsEmpty() {
    Restaurant emptyMenuRestaurant = new Restaurant(2L, "Empty", new RestaurantMenu(Collections.emptyList()));

    assertFalse(emptyMenuRestaurant.findMenuItem("1").isPresent());
  }

  @Test(expected = UnsupportedOperationException.class)
  public void shouldThrowOnReviseMenu() {
    restaurant.reviseMenu(new RestaurantMenu(Collections.singletonList(chickenVindaloo)));
  }
}
