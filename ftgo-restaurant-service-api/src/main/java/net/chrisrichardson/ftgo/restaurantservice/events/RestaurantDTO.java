package net.chrisrichardson.ftgo.restaurantservice.events;

import net.chrisrichardson.ftgo.common.Address;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import java.util.List;

/**
 * Shared representation of a Restaurant returned by the Restaurant microservice
 * REST API (GET /restaurants/{id}). Used both by the microservice and by the
 * Order service's HTTP client so the two sides agree on the contract.
 */
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

  @Override
  public boolean equals(Object o) {
    return EqualsBuilder.reflectionEquals(this, o);
  }

  @Override
  public int hashCode() {
    return HashCodeBuilder.reflectionHashCode(this);
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this);
  }
}
