package net.chrisrichardson.ftgo.domain.events;

import net.chrisrichardson.ftgo.common.events.DomainEvent;
import net.chrisrichardson.ftgo.domain.Order;
import net.chrisrichardson.ftgo.domain.OrderState;

import java.time.Instant;

/**
 * Base class for events emitted by the {@link Order} aggregate.
 * Carries identifiers and primitive/value data only (no entity references) so it can be serialized to a broker.
 */
public abstract class OrderDomainEvent implements DomainEvent {

  private final Long orderId;
  private final Long consumerId;
  private final Long restaurantId;
  private final OrderState orderState;
  private final Instant occurredAt;

  protected OrderDomainEvent(Order order) {
    this.orderId = order.getId();
    this.consumerId = order.getConsumerId();
    this.restaurantId = order.getRestaurant() == null ? null : order.getRestaurant().getId();
    this.orderState = order.getOrderState();
    this.occurredAt = Instant.now();
  }

  public Long getOrderId() {
    return orderId;
  }

  public Long getConsumerId() {
    return consumerId;
  }

  public Long getRestaurantId() {
    return restaurantId;
  }

  /**
   * @return the state of the order after this event
   */
  public OrderState getOrderState() {
    return orderState;
  }

  @Override
  public Instant getOccurredAt() {
    return occurredAt;
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + "{orderId=" + orderId + ", orderState=" + orderState + ", occurredAt=" + occurredAt + "}";
  }
}
