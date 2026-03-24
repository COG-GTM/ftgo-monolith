package net.chrisrichardson.ftgo.courierservice.api;

import net.chrisrichardson.ftgo.common.Address;

import java.time.LocalDateTime;

public class ScheduleDeliveryRequest {
  private long orderId;
  private Address pickupAddress;
  private Address deliveryAddress;
  private LocalDateTime readyBy;

  public ScheduleDeliveryRequest() {
  }

  public ScheduleDeliveryRequest(long orderId, Address pickupAddress, Address deliveryAddress, LocalDateTime readyBy) {
    this.orderId = orderId;
    this.pickupAddress = pickupAddress;
    this.deliveryAddress = deliveryAddress;
    this.readyBy = readyBy;
  }

  public long getOrderId() {
    return orderId;
  }

  public void setOrderId(long orderId) {
    this.orderId = orderId;
  }

  public Address getPickupAddress() {
    return pickupAddress;
  }

  public void setPickupAddress(Address pickupAddress) {
    this.pickupAddress = pickupAddress;
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
