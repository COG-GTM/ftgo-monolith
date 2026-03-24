package net.chrisrichardson.ftgo.orderservice.web;

import net.chrisrichardson.ftgo.common.Money;

import java.util.List;

public class GetOrderResponse {
  private long orderId;
  private String state;
  private Money orderTotal;
  private String restaurantName;
  private Long assignedCourierId;
  private List<String> courierActions;

  private GetOrderResponse() {
  }

  public Long getAssignedCourierId() {
    return assignedCourierId;
  }

  public void setAssignedCourierId(Long assignedCourierId) {
    this.assignedCourierId = assignedCourierId;
  }

  public GetOrderResponse(long orderId, String state, Money orderTotal, String restaurantName, Long assignedCourierId, List<String> courierActions) {
    this.orderId = orderId;
    this.state = state;
    this.orderTotal = orderTotal;
    this.restaurantName = restaurantName;
    this.assignedCourierId = assignedCourierId;
    this.courierActions = courierActions;
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

  public String getRestaurantName() {
    return restaurantName;
  }

  public List<String> getCourierActions() {
    return courierActions;
  }

  public void setCourierActions(List<String> courierActions) {
    this.courierActions = courierActions;
  }
}
