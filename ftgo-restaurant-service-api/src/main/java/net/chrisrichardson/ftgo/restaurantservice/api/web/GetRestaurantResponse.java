package net.chrisrichardson.ftgo.restaurantservice.api.web;

import net.chrisrichardson.ftgo.restaurantservice.events.MenuItemDTO;

import java.util.List;

public class GetRestaurantResponse {
  private long restaurantId;
  private String name;
  private List<MenuItemDTO> menuItems;

  private GetRestaurantResponse() {
  }

  public GetRestaurantResponse(long restaurantId, String name, List<MenuItemDTO> menuItems) {
    this.restaurantId = restaurantId;
    this.name = name;
    this.menuItems = menuItems;
  }

  public long getRestaurantId() {
    return restaurantId;
  }

  public void setRestaurantId(long restaurantId) {
    this.restaurantId = restaurantId;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public List<MenuItemDTO> getMenuItems() {
    return menuItems;
  }

  public void setMenuItems(List<MenuItemDTO> menuItems) {
    this.menuItems = menuItems;
  }
}
