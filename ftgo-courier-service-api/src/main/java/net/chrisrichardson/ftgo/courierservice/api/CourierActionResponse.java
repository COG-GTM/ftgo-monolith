package net.chrisrichardson.ftgo.courierservice.api;

import java.time.LocalDateTime;

public class CourierActionResponse {

  private String type;
  private LocalDateTime time;

  public CourierActionResponse() {
  }

  public CourierActionResponse(String type, LocalDateTime time) {
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
