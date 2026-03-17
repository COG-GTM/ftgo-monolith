package net.chrisrichardson.ftgo.domain.dto;

import java.time.LocalDateTime;

/**
 * Cross-service DTO representing a courier delivery action.
 * Used by Order Service for tracking delivery progress.
 */
public class DeliveryActionDTO {

  private String type;
  private long orderId;
  private LocalDateTime time;

  public DeliveryActionDTO() {
  }

  public DeliveryActionDTO(String type, long orderId, LocalDateTime time) {
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

  public long getOrderId() {
    return orderId;
  }

  public void setOrderId(long orderId) {
    this.orderId = orderId;
  }

  public LocalDateTime getTime() {
    return time;
  }

  public void setTime(LocalDateTime time) {
    this.time = time;
  }
}
