package net.chrisrichardson.ftgo.domain.dto;

import net.chrisrichardson.ftgo.common.Address;

/**
 * Cross-service DTO representing a Restaurant.
 * Used by Order Service for order creation and display.
 */
public class RestaurantDTO {

  private long id;
  private String name;
  private Address address;

  public RestaurantDTO() {
  }

  public RestaurantDTO(long id, String name, Address address) {
    this.id = id;
    this.name = name;
    this.address = address;
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
}
