package net.chrisrichardson.ftgo.restaurantservice.domain;

import net.chrisrichardson.ftgo.restaurantservice.api.web.GetRestaurantResponse;
import net.chrisrichardson.ftgo.restaurantservice.events.MenuItemDTO;
import net.chrisrichardson.ftgo.restaurantservice.events.RestaurantMenuDTO;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public final class RestaurantResponses {

  private RestaurantResponses() {
  }

  public static GetRestaurantResponse toGetRestaurantResponse(Restaurant restaurant) {
    List<MenuItemDTO> menuItems = restaurant.getMenuItems() == null
            ? Collections.emptyList()
            : restaurant.getMenuItems().stream()
                .map(mi -> new MenuItemDTO(mi.getId(), mi.getName(), mi.getPrice()))
                .collect(Collectors.toList());
    return new GetRestaurantResponse(restaurant.getId(), restaurant.getName(), restaurant.getAddress(),
            new RestaurantMenuDTO(menuItems));
  }
}
