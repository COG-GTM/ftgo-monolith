package net.chrisrichardson.ftgo.courierservice.api;

import java.time.LocalDateTime;

public class ScheduleDeliveryRequest {
  private long orderId;
  private LocalDateTime readyBy;

  private ScheduleDeliveryRequest() {
  }

  public ScheduleDeliveryRequest(long orderId, LocalDateTime readyBy) {
    this.orderId = orderId;
    this.readyBy = readyBy;
  }

  public long getOrderId() {
    return orderId;
  }

  public void setOrderId(long orderId) {
    this.orderId = orderId;
  }

  public LocalDateTime getReadyBy() {
    return readyBy;
  }

  public void setReadyBy(LocalDateTime readyBy) {
    this.readyBy = readyBy;
  }
}
