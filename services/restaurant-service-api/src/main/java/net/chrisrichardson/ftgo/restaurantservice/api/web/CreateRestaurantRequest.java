package net.chrisrichardson.ftgo.restaurantservice.api.web;

import net.chrisrichardson.ftgo.common.Address;

import java.util.List;

/**
 * Request DTO for creating a new restaurant.
 *
 * <p>Service ownership: Restaurant Service</p>
 */
public class CreateRestaurantRequest {

  private String name;
  private Address address;
  private List<MenuItemDTO> menuItems;

  private CreateRestaurantRequest() {
  }

  public CreateRestaurantRequest(String name, Address address, List<MenuItemDTO> menuItems) {
    this.name = name;
    this.address = address;
    this.menuItems = menuItems;
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

  public List<MenuItemDTO> getMenuItems() {
    return menuItems;
  }

  public void setMenuItems(List<MenuItemDTO> menuItems) {
    this.menuItems = menuItems;
  }
}
