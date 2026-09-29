package net.chrisrichardson.ftgo.domain.events;

import net.chrisrichardson.ftgo.domain.OrderState;

public class OrderPreparingEvent extends OrderStateChangedEvent {

  public OrderPreparingEvent(OrderState previousState) {
    super(previousState, OrderState.PREPARING);
  }
}
