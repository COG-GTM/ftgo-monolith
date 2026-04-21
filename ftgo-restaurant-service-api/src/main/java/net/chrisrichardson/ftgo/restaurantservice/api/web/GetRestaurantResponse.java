package net.chrisrichardson.ftgo.restaurantservice.api.web;

import net.chrisrichardson.ftgo.restaurantservice.events.MenuItemDTO;

import java.util.List;

public class GetRestaurantResponse {
  private long id;
  private String name;
  private List<MenuItemDTO> menuItems;

  private GetRestaurantResponse() {
  }

  public GetRestaurantResponse(long id, String name, List<MenuItemDTO> menuItems) {
    this.id = id;
    this.name = name;
    this.menuItems = menuItems;
  }

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
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
