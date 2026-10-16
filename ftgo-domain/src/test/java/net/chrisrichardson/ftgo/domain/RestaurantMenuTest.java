package net.chrisrichardson.ftgo.domain;

import net.chrisrichardson.ftgo.common.Money;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.*;

public class RestaurantMenuTest {

  @Test
  public void reflectionEqualityAndHashCodeUseMenuItems() {
    RestaurantMenu first = menu(item("soup", "Soup", "4.50"));
    RestaurantMenu same = menu(item("soup", "Soup", "4.50"));
    RestaurantMenu different = menu(item("salad", "Soup", "4.50"));

    assertEquals(first, same);
    assertEquals(first.hashCode(), same.hashCode());
    assertNotEquals(first, different);
  }

  @Test
  public void toStringIncludesMenuItemValues() {
    RestaurantMenu menu = menu(item("soup", "Tomato Soup", "4.50"));

    assertTrue(menu.toString().contains("Tomato Soup"));
    assertTrue(menu.toString().contains("4.50"));
  }

  @Test
  public void setterChangesEquality() {
    RestaurantMenu menu = menu(item("soup", "Soup", "4.50"));
    RestaurantMenu original = menu(item("soup", "Soup", "4.50"));

    menu.setMenuItems(Collections.emptyList());

    assertNotEquals(original, menu);
  }

  private static RestaurantMenu menu(MenuItem... items) {
    return new RestaurantMenu(Arrays.asList(items));
  }

  private static MenuItem item(String id, String name, String price) {
    return new MenuItem(id, name, new Money(price));
  }
}
