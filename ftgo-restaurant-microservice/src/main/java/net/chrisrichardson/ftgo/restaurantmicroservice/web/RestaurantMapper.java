package net.chrisrichardson.ftgo.restaurantmicroservice.web;

import net.chrisrichardson.ftgo.restaurantmicroservice.domain.MenuItem;
import net.chrisrichardson.ftgo.restaurantmicroservice.domain.Restaurant;
import net.chrisrichardson.ftgo.restaurantservice.events.MenuItemDTO;
import net.chrisrichardson.ftgo.restaurantservice.events.RestaurantDTO;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Maps the internal {@link Restaurant} JPA entity to the public
 * {@link RestaurantDTO} contract exposed over the REST API.
 */
public final class RestaurantMapper {

  private RestaurantMapper() {
  }

  public static RestaurantDTO toRestaurantDTO(Restaurant restaurant) {
    List<MenuItemDTO> menuItems = restaurant.getMenuItems() == null
            ? null
            : restaurant.getMenuItems().stream()
            .map(RestaurantMapper::toMenuItemDTO)
            .collect(Collectors.toList());
    return new RestaurantDTO(restaurant.getId(), restaurant.getName(), restaurant.getAddress(), menuItems);
  }

  private static MenuItemDTO toMenuItemDTO(MenuItem menuItem) {
    return new MenuItemDTO(menuItem.getId(), menuItem.getName(), menuItem.getPrice());
  }
}
