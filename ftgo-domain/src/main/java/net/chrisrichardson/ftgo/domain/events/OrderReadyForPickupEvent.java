package net.chrisrichardson.ftgo.domain.events;

import net.chrisrichardson.ftgo.domain.Order;
import net.chrisrichardson.ftgo.domain.OrderState;

public class OrderReadyForPickupEvent extends OrderStateTransitionEvent {

  public OrderReadyForPickupEvent(Order order, OrderState previousState) {
    super(order, previousState);
  }
}
