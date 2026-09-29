package net.chrisrichardson.ftgo.domain.events;

import net.chrisrichardson.ftgo.domain.OrderState;

public class OrderDeliveredEvent extends OrderStateChangedEvent {

  public OrderDeliveredEvent(OrderState previousState) {
    super(previousState, OrderState.DELIVERED);
  }
}
