package net.chrisrichardson.ftgo.courierservice.api;

import java.time.LocalDateTime;

public class CourierAssignmentResponse {

  private Long courierId;
  private LocalDateTime estimatedDeliveryTime;

  public CourierAssignmentResponse() {
  }

  public CourierAssignmentResponse(Long courierId, LocalDateTime estimatedDeliveryTime) {
    this.courierId = courierId;
    this.estimatedDeliveryTime = estimatedDeliveryTime;
  }

  public Long getCourierId() {
    return courierId;
  }

  public void setCourierId(Long courierId) {
    this.courierId = courierId;
  }

  public LocalDateTime getEstimatedDeliveryTime() {
    return estimatedDeliveryTime;
  }

  public void setEstimatedDeliveryTime(LocalDateTime estimatedDeliveryTime) {
    this.estimatedDeliveryTime = estimatedDeliveryTime;
  }
}
