package net.chrisrichardson.ftgo.domain.events;

import net.chrisrichardson.ftgo.common.Money;
import net.chrisrichardson.ftgo.domain.Order;

public class OrderCreatedEvent extends OrderDomainEvent {

  private final Money orderTotal;

  public OrderCreatedEvent(Order order) {
    super(order);
    this.orderTotal = order.getOrderTotal();
  }

  public Money getOrderTotal() {
    return orderTotal;
  }
}
