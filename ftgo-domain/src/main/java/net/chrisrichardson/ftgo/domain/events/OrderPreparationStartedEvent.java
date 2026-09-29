package net.chrisrichardson.ftgo.domain.events;

import net.chrisrichardson.ftgo.domain.Order;
import net.chrisrichardson.ftgo.domain.OrderState;

public class OrderPreparationStartedEvent extends OrderStateTransitionEvent {

  public OrderPreparationStartedEvent(Order order, OrderState previousState) {
    super(order, previousState);
  }
}
