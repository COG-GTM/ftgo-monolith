package net.chrisrichardson.ftgo.orderservice.api.web;

import java.time.LocalDateTime;

import javax.validation.constraints.NotNull;

public class OrderAcceptance {
  @NotNull
  private LocalDateTime readyBy;

  public OrderAcceptance() {
  }

  public OrderAcceptance(LocalDateTime readyBy) {
    this.readyBy = readyBy;
  }

  public LocalDateTime getReadyBy() {
    return readyBy;
  }

  public void setReadyBy(LocalDateTime readyBy) {
    this.readyBy = readyBy;
  }
}
