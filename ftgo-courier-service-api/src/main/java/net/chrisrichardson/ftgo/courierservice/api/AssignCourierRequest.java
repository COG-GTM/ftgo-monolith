package net.chrisrichardson.ftgo.courierservice.api;

import java.time.LocalDateTime;

public class AssignCourierRequest {
  private Long orderId;
  private LocalDateTime pickupTime;
  private LocalDateTime dropoffTime;

  public AssignCourierRequest() {
  }

  public AssignCourierRequest(Long orderId, LocalDateTime pickupTime, LocalDateTime dropoffTime) {
    this.orderId = orderId;
    this.pickupTime = pickupTime;
    this.dropoffTime = dropoffTime;
  }

  public Long getOrderId() {
    return orderId;
  }

  public void setOrderId(Long orderId) {
    this.orderId = orderId;
  }

  public LocalDateTime getPickupTime() {
    return pickupTime;
  }

  public void setPickupTime(LocalDateTime pickupTime) {
    this.pickupTime = pickupTime;
  }

  public LocalDateTime getDropoffTime() {
    return dropoffTime;
  }

  public void setDropoffTime(LocalDateTime dropoffTime) {
    this.dropoffTime = dropoffTime;
  }
}
