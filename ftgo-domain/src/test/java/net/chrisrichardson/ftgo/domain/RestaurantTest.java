package net.chrisrichardson.ftgo.domain;

import net.chrisrichardson.ftgo.common.Address;
import net.chrisrichardson.ftgo.common.Money;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.junit.Assert.*;

public class RestaurantTest {

  @Test
  public void findsMenuItemAndReturnsEmptyForUnknownId() {
    MenuItem soup = new MenuItem("soup", "Soup", new Money("4.50"));
    Restaurant restaurant = restaurantWithMenu(soup);

    assertEquals(Optional.of(soup), restaurant.findMenuItem("soup"));
    assertEquals(Optional.empty(), restaurant.findMenuItem("unknown"));
  }

  @Test
  public void idNameMenuConstructorSetsIdAndLeavesAddressNull() {
    Restaurant restaurant = new Restaurant(42L, "Cafe", menu());

    assertEquals(Long.valueOf(42L), restaurant.getId());
    assertEquals("Cafe", restaurant.getName());
    assertNull(restaurant.getAddress());
  }

  @Test
  public void nameAddressMenuConstructorKeepsAddress() {
    Address address = new Address("1 Main St", null, "Oakland", "CA", "94612");

    Restaurant restaurant = new Restaurant("Cafe", address, menu());

    assertSame(address, restaurant.getAddress());
  }

  @Test(expected = UnsupportedOperationException.class)
  public void reviseMenuIsUnsupported() {
    restaurantWithMenu().reviseMenu(menu());
  }

  @Test(expected = NullPointerException.class)
  public void findMenuItemCurrentlyThrowsNpeWhenCreatedWithDefaultConstructor() {
    // Suspected bug: the default constructor leaves menuItems null.
    new Restaurant().findMenuItem("soup");
  }

  @Test
  public void sharesTheRestaurantMenusList() {
    ArrayList<MenuItem> menuItems = new ArrayList<>();
    RestaurantMenu restaurantMenu = new RestaurantMenu(menuItems);
    Restaurant restaurant = new Restaurant("Cafe", null, restaurantMenu);
    MenuItem addedLater = new MenuItem("soup", "Soup", new Money(5));

    restaurantMenu.getMenuItems().add(addedLater);

    assertEquals(Optional.of(addedLater), restaurant.findMenuItem("soup"));
  }

  private static Restaurant restaurantWithMenu(MenuItem... items) {
    return new Restaurant("Cafe", null, new RestaurantMenu(Arrays.asList(items)));
  }

  private static RestaurantMenu menu() {
    return new RestaurantMenu(Collections.emptyList());
  }
}
