package net.chrisrichardson.ftgo.consumerservice.api.web;

import net.chrisrichardson.ftgo.common.Money;

public class ValidateOrderForConsumerRequest {
  private long consumerId;
  private Money orderTotal;

  public ValidateOrderForConsumerRequest() {
  }

  public ValidateOrderForConsumerRequest(long consumerId, Money orderTotal) {
    this.consumerId = consumerId;
    this.orderTotal = orderTotal;
  }

  public long getConsumerId() {
    return consumerId;
  }

  public void setConsumerId(long consumerId) {
    this.consumerId = consumerId;
  }

  public Money getOrderTotal() {
    return orderTotal;
  }

  public void setOrderTotal(Money orderTotal) {
    this.orderTotal = orderTotal;
  }
}
