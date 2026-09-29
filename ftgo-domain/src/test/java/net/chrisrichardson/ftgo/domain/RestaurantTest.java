package net.chrisrichardson.ftgo.domain;

import net.chrisrichardson.ftgo.common.Address;
import net.chrisrichardson.ftgo.common.Money;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.junit.Assert.*;

public class RestaurantTest {

  private MenuItem chickenVindaloo;
  private MenuItem naan;
  private Restaurant restaurant;

  @Before
  public void setUp() {
    chickenVindaloo = new MenuItem("1", "Chicken Vindaloo", new Money("12.34"));
    naan = new MenuItem("2", "Naan", new Money("3.00"));
    restaurant = new Restaurant("Ajanta", new Address("1 Main St", null, "Oakland", "CA", "94612"),
            new RestaurantMenu(Arrays.asList(chickenVindaloo, naan)));
  }

  @Test
  public void shouldFindMenuItemById() {
    Optional<MenuItem> found = restaurant.findMenuItem("2");

    assertTrue(found.isPresent());
    assertEquals(naan, found.get());
  }

  @Test
  public void shouldReturnEmptyForUnknownMenuItem() {
    assertFalse(restaurant.findMenuItem("999").isPresent());
  }

  @Test(expected = UnsupportedOperationException.class)
  public void shouldThrowOnReviseMenu() {
    restaurant.reviseMenu(new RestaurantMenu(Collections.singletonList(chickenVindaloo)));
  }
}
