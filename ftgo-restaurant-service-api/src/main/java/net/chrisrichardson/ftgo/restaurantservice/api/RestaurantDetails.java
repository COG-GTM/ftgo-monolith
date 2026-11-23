package net.chrisrichardson.ftgo.restaurantservice.api;

import net.chrisrichardson.ftgo.common.Address;
import net.chrisrichardson.ftgo.restaurantservice.events.MenuItemDTO;
import net.chrisrichardson.ftgo.restaurantservice.events.RestaurantMenuDTO;

import java.util.Optional;

public class RestaurantDetails {
  private long id;
  private String name;
  private Address address;
  private RestaurantMenuDTO menu;

  public RestaurantDetails() {
  }

  public RestaurantDetails(long id, String name, Address address, RestaurantMenuDTO menu) {
    this.id = id;
    this.name = name;
    this.address = address;
    this.menu = menu;
  }

  public Optional<MenuItemDTO> findMenuItem(String menuItemId) {
    return menu.getMenuItemDTOs().stream().filter(mi -> mi.getId().equals(menuItemId)).findFirst();
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

  public Address getAddress() {
    return address;
  }

  public void setAddress(Address address) {
    this.address = address;
  }

  public RestaurantMenuDTO getMenu() {
    return menu;
  }

  public void setMenu(RestaurantMenuDTO menu) {
    this.menu = menu;
  }
}
