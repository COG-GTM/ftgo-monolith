package net.chrisrichardson.ftgo.restaurantservice.api.events;

import net.chrisrichardson.ftgo.common.Address;

import java.util.List;

/**
 * Event DTO published when a restaurant is created.
 * Used for cross-service event-driven communication.
 *
 * <p>Service ownership: Restaurant Service</p>
 */
public class RestaurantCreatedEvent {

  private long restaurantId;
  private String name;
  private Address address;

  private RestaurantCreatedEvent() {
  }

  public RestaurantCreatedEvent(long restaurantId, String name, Address address) {
    this.restaurantId = restaurantId;
    this.name = name;
    this.address = address;
  }

  public long getRestaurantId() {
    return restaurantId;
  }

  public void setRestaurantId(long restaurantId) {
    this.restaurantId = restaurantId;
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
