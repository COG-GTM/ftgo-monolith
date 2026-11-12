package net.chrisrichardson.ftgo.courierservice.api;

import java.time.LocalDateTime;

public class CourierAction {

  private String type;
  private Long orderId;
  private LocalDateTime time;

  public CourierAction() {
  }

  public CourierAction(String type, Long orderId, LocalDateTime time) {
    this.type = type;
    this.orderId = orderId;
    this.time = time;
  }

  public String getType() {
    return type;
  }

  public Long getOrderId() {
    return orderId;
  }

  public LocalDateTime getTime() {
    return time;
  }
}
