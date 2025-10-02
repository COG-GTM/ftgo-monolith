package net.chrisrichardson.ftgo.orderservice.restaurant;

import net.chrisrichardson.ftgo.orderservice.restaurant.RestaurantServiceProxy.MenuItemInfo;

import java.util.List;
import java.util.Optional;

public class RestaurantInfo {
  private final Long id;
  private final String name;
  private final List<MenuItemInfo> menuItems;

  public RestaurantInfo(Long id, String name, List<MenuItemInfo> menuItems) {
    this.id = id;
    this.name = name;
    this.menuItems = menuItems;
  }

  public Long getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public List<MenuItemInfo> getMenuItems() {
    return menuItems;
  }

  public Optional<MenuItemInfo> findMenuItem(String menuItemId) {
    return menuItems.stream()
            .filter(mi -> mi.getId().equals(menuItemId))
            .findFirst();
  }
}
