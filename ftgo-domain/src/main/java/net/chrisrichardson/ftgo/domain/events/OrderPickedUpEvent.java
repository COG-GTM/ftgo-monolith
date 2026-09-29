package net.chrisrichardson.ftgo.domain.events;

import net.chrisrichardson.ftgo.domain.OrderState;

public class OrderPickedUpEvent extends OrderStateChangedEvent {

  public OrderPickedUpEvent(OrderState previousState) {
    super(previousState, OrderState.PICKED_UP);
  }
}
