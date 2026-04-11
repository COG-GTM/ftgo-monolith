package net.chrisrichardson.ftgo.orderservice.web;

import net.chrisrichardson.ftgo.common.Money;

public class GetOrderResponse {
  private long orderId;
  private String state;
  private Money orderTotal;
  private Long restaurantId;
  private Long assignedCourierId;

  private GetOrderResponse() {
  }

  public Long getAssignedCourierId() {
    return assignedCourierId;
  }

  public void setAssignedCourierId(Long assignedCourierId) {
    this.assignedCourierId = assignedCourierId;
  }

  public GetOrderResponse(long orderId, String state, Money orderTotal, Long restaurantId, Long assignedCourierId) {
    this.orderId = orderId;
    this.state = state;
    this.orderTotal = orderTotal;
    this.restaurantId = restaurantId;
    this.assignedCourierId = assignedCourierId;
  }

  public Money getOrderTotal() {
    return orderTotal;
  }

  public void setOrderTotal(Money orderTotal) {
    this.orderTotal = orderTotal;
  }

  public long getOrderId() {
    return orderId;
  }

  public void setOrderId(long orderId) {
    this.orderId = orderId;
  }

  public String getState() {
    return state;
  }

  public void setState(String state) {
    this.state = state;
  }

  public Long getRestaurantId() {
    return restaurantId;
  }

  public void setRestaurantId(Long restaurantId) {
    this.restaurantId = restaurantId;
  }
}
