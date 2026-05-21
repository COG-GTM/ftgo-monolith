package net.chrisrichardson.ftgo.courierservice.api;

import java.time.LocalDateTime;
import java.util.List;

public class CourierAssignmentResponse {

  private long courierId;
  private LocalDateTime estimatedDeliveryTime;
  private List<CourierActionDTO> actions;

  public CourierAssignmentResponse() {
  }

  public CourierAssignmentResponse(long courierId, LocalDateTime estimatedDeliveryTime, List<CourierActionDTO> actions) {
    this.courierId = courierId;
    this.estimatedDeliveryTime = estimatedDeliveryTime;
    this.actions = actions;
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

  public List<CourierActionDTO> getActions() {
    return actions;
  }

  public void setActions(List<CourierActionDTO> actions) {
    this.actions = actions;
  }
}
