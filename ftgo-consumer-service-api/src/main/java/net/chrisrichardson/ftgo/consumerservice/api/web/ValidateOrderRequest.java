package net.chrisrichardson.ftgo.consumerservice.api.web;

public class ValidateOrderRequest {

  private String orderTotal;

  private ValidateOrderRequest() {
  }

  public ValidateOrderRequest(String orderTotal) {
    this.orderTotal = orderTotal;
  }

  public String getOrderTotal() {
    return orderTotal;
  }

  public void setOrderTotal(String orderTotal) {
    this.orderTotal = orderTotal;
  }
}
