package net.chrisrichardson.ftgo.domain.events;

import net.chrisrichardson.ftgo.domain.Order;
import net.chrisrichardson.ftgo.domain.OrderState;

public class OrderDeliveredEvent extends OrderStateTransitionEvent {

  public OrderDeliveredEvent(Order order, OrderState previousState) {
    super(order, previousState);
  }
}
