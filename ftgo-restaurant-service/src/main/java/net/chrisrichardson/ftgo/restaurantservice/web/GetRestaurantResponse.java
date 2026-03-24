package net.chrisrichardson.ftgo.restaurantservice.web;

import net.chrisrichardson.ftgo.domain.MenuItem;

import java.util.List;

public class GetRestaurantResponse {
  private Long id;
  private String name;
  private List<MenuItem> menuItems;

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

  public List<MenuItem> getMenuItems() {
    return menuItems;
  }

  public void setMenuItems(List<MenuItem> menuItems) {
    this.menuItems = menuItems;
  }

  public GetRestaurantResponse() {
  }

  public GetRestaurantResponse(Long id, String name, List<MenuItem> menuItems) {
    this.id = id;
    this.name = name;
    this.menuItems = menuItems;
  }
}
