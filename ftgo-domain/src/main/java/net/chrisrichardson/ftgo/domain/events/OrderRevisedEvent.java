package net.chrisrichardson.ftgo.domain.events;

import net.chrisrichardson.ftgo.common.Money;

public class OrderRevisedEvent extends OrderDomainEvent {

  private final Money orderTotal;

  public OrderRevisedEvent(Money orderTotal) {
    this.orderTotal = orderTotal;
  }

  public Money getOrderTotal() {
    return orderTotal;
  }

  @Override
  public String toString() {
    return "OrderRevisedEvent{orderTotal=" + orderTotal + ", occurredAt=" + getOccurredAt() + "}";
  }
}
