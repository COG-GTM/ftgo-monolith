package net.chrisrichardson.ftgo.domain.events;

import net.chrisrichardson.ftgo.domain.OrderState;

public class OrderCancelledEvent extends OrderStateChangedEvent {

  public OrderCancelledEvent(OrderState previousState) {
    super(previousState, OrderState.CANCELLED);
  }
}
