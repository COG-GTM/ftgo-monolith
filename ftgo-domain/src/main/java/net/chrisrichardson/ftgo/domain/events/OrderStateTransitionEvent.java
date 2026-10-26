package net.chrisrichardson.ftgo.domain.events;

import net.chrisrichardson.ftgo.domain.Order;
import net.chrisrichardson.ftgo.domain.OrderState;

/**
 * An event emitted when an existing order moves from one {@link OrderState} to another.
 */
public abstract class OrderStateTransitionEvent extends OrderDomainEvent {

  private final OrderState previousState;

  protected OrderStateTransitionEvent(Order order, OrderState previousState) {
    super(order);
    this.previousState = previousState;
  }

  public OrderState getPreviousState() {
    return previousState;
  }
}
