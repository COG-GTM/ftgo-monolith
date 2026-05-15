package net.chrisrichardson.ftgo.courierservice.api;

import java.time.LocalDateTime;

public class CourierActionDTO {

  private String type;
  private LocalDateTime time;
  private long orderId;

  public CourierActionDTO() {
  }

  public CourierActionDTO(String type, LocalDateTime time, long orderId) {
    this.type = type;
    this.time = time;
    this.orderId = orderId;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public LocalDateTime getTime() {
    return time;
  }

  public void setTime(LocalDateTime time) {
    this.time = time;
  }

  public long getOrderId() {
    return orderId;
  }

  public void setOrderId(long orderId) {
    this.orderId = orderId;
  }
}
