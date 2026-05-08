package net.chrisrichardson.ftgo.courierservice.api;

import net.chrisrichardson.ftgo.common.Address;

import java.time.LocalDateTime;

public class CourierAssignmentRequest {

  private Long orderId;
  private Address restaurantAddress;
  private Address deliveryAddress;
  private LocalDateTime readyBy;

  public CourierAssignmentRequest() {
  }

  public CourierAssignmentRequest(Long orderId, Address restaurantAddress, Address deliveryAddress, LocalDateTime readyBy) {
    this.orderId = orderId;
    this.restaurantAddress = restaurantAddress;
    this.deliveryAddress = deliveryAddress;
    this.readyBy = readyBy;
  }

  public Long getOrderId() {
    return orderId;
  }

  public void setOrderId(Long orderId) {
    this.orderId = orderId;
  }

  public Address getRestaurantAddress() {
    return restaurantAddress;
  }

  public void setRestaurantAddress(Address restaurantAddress) {
    this.restaurantAddress = restaurantAddress;
  }

  public Address getDeliveryAddress() {
    return deliveryAddress;
  }

  public void setDeliveryAddress(Address deliveryAddress) {
    this.deliveryAddress = deliveryAddress;
  }

  public LocalDateTime getReadyBy() {
    return readyBy;
  }

  public void setReadyBy(LocalDateTime readyBy) {
    this.readyBy = readyBy;
  }
}
