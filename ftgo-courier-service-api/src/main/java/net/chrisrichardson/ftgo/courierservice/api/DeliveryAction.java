package net.chrisrichardson.ftgo.courierservice.api;

import java.time.LocalDateTime;

public class DeliveryAction {
  private DeliveryActionType type;
  private LocalDateTime time;

  public DeliveryAction() {
  }

  public DeliveryAction(DeliveryActionType type, LocalDateTime time) {
    this.type = type;
    this.time = time;
  }

  public DeliveryActionType getType() {
    return type;
  }

  public void setType(DeliveryActionType type) {
    this.type = type;
  }

  public LocalDateTime getTime() {
    return time;
  }

  public void setTime(LocalDateTime time) {
    this.time = time;
  }
}
