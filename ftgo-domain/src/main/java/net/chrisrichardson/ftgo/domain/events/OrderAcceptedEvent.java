package net.chrisrichardson.ftgo.domain.events;

import net.chrisrichardson.ftgo.domain.OrderState;

import java.time.LocalDateTime;

public class OrderAcceptedEvent extends OrderStateChangedEvent {

  private final LocalDateTime readyBy;

  public OrderAcceptedEvent(OrderState previousState, LocalDateTime readyBy) {
    super(previousState, OrderState.ACCEPTED);
    this.readyBy = readyBy;
  }

  public LocalDateTime getReadyBy() {
    return readyBy;
  }
}
