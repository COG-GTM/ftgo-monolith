package net.chrisrichardson.ftgo.restaurantservice.web;

import net.chrisrichardson.ftgo.restaurantservice.events.MenuItemDTO;

import java.util.List;

public class GetRestaurantResponse {
  private Long id;
  private String name;
  private List<MenuItemDTO> menuItems;

  public GetRestaurantResponse() {
  }

  public GetRestaurantResponse(Long id, String name, List<MenuItemDTO> menuItems) {
    this.id = id;
    this.name = name;
    this.menuItems = menuItems;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
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
