package net.chrisrichardson.ftgo.orderservice.domain.client;

import net.chrisrichardson.ftgo.common.Address;

import java.util.List;

public class RestaurantDTO {
  private Long id;
  private String name;
  private Address address;
  private List<MenuItemDTO> menuItems;

  public RestaurantDTO() {
  }

  public RestaurantDTO(Long id, String name, Address address, List<MenuItemDTO> menuItems) {
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

  public List<MenuItemDTO> getMenuItems() {
    return menuItems;
  }

  public void setMenuItems(List<MenuItemDTO> menuItems) {
    this.menuItems = menuItems;
  }
}
