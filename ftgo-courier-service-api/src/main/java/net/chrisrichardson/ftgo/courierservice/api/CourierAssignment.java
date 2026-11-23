package net.chrisrichardson.ftgo.courierservice.api;

import java.time.LocalDateTime;

public class CourierAssignment {
  private long courierId;
  private LocalDateTime estimatedDeliveryTime;

  public CourierAssignment() {
  }

  public CourierAssignment(long courierId, LocalDateTime estimatedDeliveryTime) {
    this.courierId = courierId;
    this.estimatedDeliveryTime = estimatedDeliveryTime;
  }

  public long getCourierId() {
    return courierId;
  }

  public void setCourierId(long courierId) {
    this.courierId = courierId;
  }

  public LocalDateTime getEstimatedDeliveryTime() {
    return estimatedDeliveryTime;
  }

  public void setEstimatedDeliveryTime(LocalDateTime estimatedDeliveryTime) {
    this.estimatedDeliveryTime = estimatedDeliveryTime;
  }
}
