package net.chrisrichardson.ftgo.restaurantservice.web;

import net.chrisrichardson.ftgo.common.Address;

import java.util.List;

public class GetRestaurantResponse {
  private Long id;
  private String name;
  private Address address;
  private List<MenuItemResponse> menuItems;

  public GetRestaurantResponse() {
  }

  public GetRestaurantResponse(Long id, String name, Address address, List<MenuItemResponse> menuItems) {
    this.id = id;
    this.name = name;
    this.address = address;
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

  public Address getAddress() {
    return address;
  }

  public void setAddress(Address address) {
    this.address = address;
  }

  public List<MenuItemResponse> getMenuItems() {
    return menuItems;
  }

  public void setMenuItems(List<MenuItemResponse> menuItems) {
    this.menuItems = menuItems;
  }
}
