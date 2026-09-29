package net.chrisrichardson.ftgo.domain.events;

import net.chrisrichardson.ftgo.common.Money;
import net.chrisrichardson.ftgo.domain.OrderState;

public class OrderCreatedEvent extends OrderDomainEvent {

  private final long consumerId;
  private final Long restaurantId;
  private final Money orderTotal;
  private final OrderState state;

  public OrderCreatedEvent(long consumerId, Long restaurantId, Money orderTotal, OrderState state) {
    this.consumerId = consumerId;
    this.restaurantId = restaurantId;
    this.orderTotal = orderTotal;
    this.state = state;
  }

  public long getConsumerId() {
    return consumerId;
  }

  public Long getRestaurantId() {
    return restaurantId;
  }

  public Money getOrderTotal() {
    return orderTotal;
  }

  public OrderState getState() {
    return state;
  }

  @Override
  public String toString() {
    return "OrderCreatedEvent{consumerId=" + consumerId + ", restaurantId=" + restaurantId +
            ", orderTotal=" + orderTotal + ", state=" + state + ", occurredAt=" + getOccurredAt() + "}";
  }
}
