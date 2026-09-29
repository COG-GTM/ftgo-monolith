package net.chrisrichardson.ftgo.domain.events;

import net.chrisrichardson.ftgo.domain.OrderState;

public class OrderReadyForPickupEvent extends OrderStateChangedEvent {

  public OrderReadyForPickupEvent(OrderState previousState) {
    super(previousState, OrderState.READY_FOR_PICKUP);
  }
}
