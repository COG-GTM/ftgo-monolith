package net.chrisrichardson.ftgo.restaurantservice.api.web;

import net.chrisrichardson.ftgo.common.Address;
import net.chrisrichardson.ftgo.restaurantservice.events.MenuItemDTO;
import net.chrisrichardson.ftgo.restaurantservice.events.RestaurantMenuDTO;

import java.util.Optional;

public class GetRestaurantResponse {
  private Long id;
  private String name;
  private Address address;
  private RestaurantMenuDTO menu;

  public GetRestaurantResponse() {
  }

  public GetRestaurantResponse(Long id, String name) {
    this.id = id;
    this.name = name;
  }

  public GetRestaurantResponse(Long id, String name, Address address, RestaurantMenuDTO menu) {
    this.id = id;
    this.name = name;
    this.address = address;
    this.menu = menu;
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

  public RestaurantMenuDTO getMenu() {
    return menu;
  }

  public void setMenu(RestaurantMenuDTO menu) {
    this.menu = menu;
  }

  public Optional<MenuItemDTO> findMenuItem(String menuItemId) {
    if (menu == null || menu.getMenuItemDTOs() == null)
      return Optional.empty();
    return menu.getMenuItemDTOs().stream().filter(mi -> menuItemId.equals(mi.getId())).findFirst();
  }
}
