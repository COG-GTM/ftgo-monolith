package net.chrisrichardson.ftgo.domain;

import net.chrisrichardson.ftgo.common.Money;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.*;

public class RestaurantMenuTest {

  @Test
  public void shouldCreateWithMenuItems() {
    RestaurantMenu menu = new RestaurantMenu(Arrays.asList(
        new MenuItem("1", "Burger", new Money("10.00")),
        new MenuItem("2", "Fries", new Money("5.00"))));
    assertEquals(2, menu.getMenuItems().size());
  }

  @Test
  public void shouldSetMenuItems() {
    RestaurantMenu menu = new RestaurantMenu(Collections.emptyList());
    menu.setMenuItems(Arrays.asList(new MenuItem("1", "Salad", new Money("8.00"))));
    assertEquals(1, menu.getMenuItems().size());
  }

  @Test
  public void shouldHaveEqualityBasedOnFields() {
    RestaurantMenu menu1 = new RestaurantMenu(Arrays.asList(
        new MenuItem("1", "Burger", new Money("10.00"))));
    RestaurantMenu menu2 = new RestaurantMenu(Arrays.asList(
        new MenuItem("1", "Burger", new Money("10.00"))));
    assertEquals(menu1, menu2);
    assertEquals(menu1.hashCode(), menu2.hashCode());
  }

  @Test
  public void shouldHaveToString() {
    RestaurantMenu menu = new RestaurantMenu(Collections.emptyList());
    assertNotNull(menu.toString());
  }
}
