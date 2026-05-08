package net.chrisrichardson.ftgo.orderservice.domain;

import net.chrisrichardson.ftgo.common.Address;

import java.time.LocalDateTime;

public class AssignCourierRequest {

  private Long orderId;
  private Address restaurantAddress;
  private LocalDateTime readyBy;

  public AssignCourierRequest() {
  }

  public AssignCourierRequest(Long orderId, Address restaurantAddress, LocalDateTime readyBy) {
    this.orderId = orderId;
    this.restaurantAddress = restaurantAddress;
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

  public LocalDateTime getReadyBy() {
    return readyBy;
  }

  public void setReadyBy(LocalDateTime readyBy) {
    this.readyBy = readyBy;
  }
}
