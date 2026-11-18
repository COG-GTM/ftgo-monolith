package net.chrisrichardson.ftgo.orderservice.web;

import net.chrisrichardson.ftgo.common.Address;
import net.chrisrichardson.ftgo.common.Money;
import net.chrisrichardson.ftgo.domain.Action;

import java.time.LocalDateTime;
import java.util.List;

public class GetOrderResponse {
  private long orderId;
  private String status;
  private Money orderTotal;
  private String restaurantName;
  private Long consumerId;
  private Address deliveryAddress;
  private String paymentToken;
  private Long assignedCourier;
  private List<Action> courierActions;
  private LocalDateTime estimatedDeliveryTime;

  private GetOrderResponse() {
  }

  public GetOrderResponse(long orderId, String status, Money orderTotal, String restaurantName,
                          Long consumerId, Address deliveryAddress, String paymentToken,
                          Long assignedCourier, List<Action> courierActions,
                          LocalDateTime estimatedDeliveryTime) {
    this.orderId = orderId;
    this.status = status;
    this.orderTotal = orderTotal;
    this.restaurantName = restaurantName;
    this.consumerId = consumerId;
    this.deliveryAddress = deliveryAddress;
    this.paymentToken = paymentToken;
    this.assignedCourier = assignedCourier;
    this.courierActions = courierActions;
    this.estimatedDeliveryTime = estimatedDeliveryTime;
  }

  public long getOrderId() {
    return orderId;
  }

  public void setOrderId(long orderId) {
    this.orderId = orderId;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public Money getOrderTotal() {
    return orderTotal;
  }

  public void setOrderTotal(Money orderTotal) {
    this.orderTotal = orderTotal;
  }

  public String getRestaurantName() {
    return restaurantName;
  }

  public Long getConsumerId() {
    return consumerId;
  }

  public Address getDeliveryAddress() {
    return deliveryAddress;
  }

  public String getPaymentToken() {
    return paymentToken;
  }

  public Long getAssignedCourier() {
    return assignedCourier;
  }

  public void setAssignedCourier(Long assignedCourier) {
    this.assignedCourier = assignedCourier;
  }

  public List<Action> getCourierActions() {
    return courierActions;
  }

  public void setCourierActions(List<Action> courierActions) {
    this.courierActions = courierActions;
  }

  public LocalDateTime getEstimatedDeliveryTime() {
    return estimatedDeliveryTime;
  }

  public void setEstimatedDeliveryTime(LocalDateTime estimatedDeliveryTime) {
    this.estimatedDeliveryTime = estimatedDeliveryTime;
  }
}
