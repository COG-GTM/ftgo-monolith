package net.chrisrichardson.ftgo.orderservice.api.web;

import java.time.LocalDateTime;

public class CourierActionDTO {
  private String type;
  private LocalDateTime time;

  public CourierActionDTO() {
  }

  public CourierActionDTO(String type, LocalDateTime time) {
    this.type = type;
    this.time = time;
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
}
