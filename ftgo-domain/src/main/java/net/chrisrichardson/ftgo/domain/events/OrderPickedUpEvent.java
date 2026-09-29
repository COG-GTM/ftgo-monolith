package net.chrisrichardson.ftgo.domain.events;

import net.chrisrichardson.ftgo.domain.Order;
import net.chrisrichardson.ftgo.domain.OrderState;

public class OrderPickedUpEvent extends OrderStateTransitionEvent {

  public OrderPickedUpEvent(Order order, OrderState previousState) {
    super(order, previousState);
  }
}
