package net.chrisrichardson.ftgo.domain.events;

import net.chrisrichardson.ftgo.domain.Order;
import net.chrisrichardson.ftgo.domain.OrderState;

public class OrderCancelledEvent extends OrderStateTransitionEvent {

  public OrderCancelledEvent(Order order, OrderState previousState) {
    super(order, previousState);
  }
}
