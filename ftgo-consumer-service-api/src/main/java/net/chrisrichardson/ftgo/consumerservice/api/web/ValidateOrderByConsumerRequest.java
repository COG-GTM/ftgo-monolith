package net.chrisrichardson.ftgo.consumerservice.api.web;

import net.chrisrichardson.ftgo.common.Money;

public class ValidateOrderByConsumerRequest {

  private Money orderTotal;

  private ValidateOrderByConsumerRequest() {
  }

  public ValidateOrderByConsumerRequest(Money orderTotal) {
    this.orderTotal = orderTotal;
  }

  public Money getOrderTotal() {
    return orderTotal;
  }

  public void setOrderTotal(Money orderTotal) {
    this.orderTotal = orderTotal;
  }
}
