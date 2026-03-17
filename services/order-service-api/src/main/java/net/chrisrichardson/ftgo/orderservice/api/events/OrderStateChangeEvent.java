package net.chrisrichardson.ftgo.orderservice.api.events;

import net.chrisrichardson.ftgo.common.Money;

import java.util.List;

/**
 * Event DTO published when an order state changes.
 * Used for cross-service event-driven communication.
 *
 * <p>Service ownership: Order Service</p>
 */
public class OrderStateChangeEvent {

  private long orderId;
  private String previousState;
  private String newState;
  private long consumerId;
  private long restaurantId;
  private Money orderTotal;

  private OrderStateChangeEvent() {
  }

  public OrderStateChangeEvent(long orderId, String previousState, String newState,
                                long consumerId, long restaurantId, Money orderTotal) {
    this.orderId = orderId;
    this.previousState = previousState;
    this.newState = newState;
    this.consumerId = consumerId;
    this.restaurantId = restaurantId;
    this.orderTotal = orderTotal;
  }

  public long getOrderId() {
    return orderId;
  }

  public void setOrderId(long orderId) {
    this.orderId = orderId;
  }

  public String getPreviousState() {
    return previousState;
  }

  public void setPreviousState(String previousState) {
    this.previousState = previousState;
  }

  public String getNewState() {
    return newState;
  }

  public void setNewState(String newState) {
    this.newState = newState;
  }

  public long getConsumerId() {
    return consumerId;
  }

  public void setConsumerId(long consumerId) {
    this.consumerId = consumerId;
  }

  public long getRestaurantId() {
    return restaurantId;
  }

  public void setRestaurantId(long restaurantId) {
    this.restaurantId = restaurantId;
  }

  public Money getOrderTotal() {
    return orderTotal;
  }

  public void setOrderTotal(Money orderTotal) {
    this.orderTotal = orderTotal;
  }
}
