package net.chrisrichardson.ftgo.orderservice.api.web;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.time.LocalDateTime;

@ApiModel(description = "Order acceptance with estimated ready time")
public class OrderAcceptance {
  @ApiModelProperty(value = "Estimated time the order will be ready", required = true)
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

