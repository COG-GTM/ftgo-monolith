package net.chrisrichardson.ftgo.domain.events;

import net.chrisrichardson.ftgo.domain.Order;
import net.chrisrichardson.ftgo.domain.OrderState;

import java.time.LocalDateTime;

public class OrderAcceptedEvent extends OrderStateTransitionEvent {

  private final LocalDateTime readyBy;

  public OrderAcceptedEvent(Order order, OrderState previousState, LocalDateTime readyBy) {
    super(order, previousState);
    this.readyBy = readyBy;
  }

  public LocalDateTime getReadyBy() {
    return readyBy;
  }
}
