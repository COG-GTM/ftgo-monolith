package net.chrisrichardson.ftgo.courierservice.api;

import java.time.LocalDateTime;

public class CourierActionDTO {
  private String type;
  private Long orderId;
  private LocalDateTime time;

  public CourierActionDTO() {
  }

  public CourierActionDTO(String type, Long orderId, LocalDateTime time) {
    this.type = type;
    this.orderId = orderId;
    this.time = time;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public Long getOrderId() {
    return orderId;
  }

  public void setOrderId(Long orderId) {
    this.orderId = orderId;
  }

  public LocalDateTime getTime() {
    return time;
  }

  public void setTime(LocalDateTime time) {
    this.time = time;
  }
}
