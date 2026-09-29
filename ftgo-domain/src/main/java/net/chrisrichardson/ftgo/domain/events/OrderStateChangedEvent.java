package net.chrisrichardson.ftgo.domain.events;

import net.chrisrichardson.ftgo.domain.OrderState;

/**
 * Emitted whenever an order moves from one {@link OrderState} to another.
 */
public abstract class OrderStateChangedEvent extends OrderDomainEvent {

  private final OrderState previousState;
  private final OrderState newState;

  protected OrderStateChangedEvent(OrderState previousState, OrderState newState) {
    this.previousState = previousState;
    this.newState = newState;
  }

  public OrderState getPreviousState() {
    return previousState;
  }

  public OrderState getNewState() {
    return newState;
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + "{" + previousState + " -> " + newState + ", occurredAt=" + getOccurredAt() + "}";
  }
}
