package net.chrisrichardson.ftgo.orderservice.domain;

import net.chrisrichardson.ftgo.common.Money;
import net.chrisrichardson.ftgo.consumerservice.domain.ConsumerService;

public class InProcessConsumerServiceProxy implements ConsumerServiceProxy {

  private final ConsumerService consumerService;

  public InProcessConsumerServiceProxy(ConsumerService consumerService) {
    this.consumerService = consumerService;
  }

  @Override
  public void validateOrderForConsumer(long consumerId, Money orderTotal) {
    consumerService.validateOrderForConsumer(consumerId, orderTotal);
  }
}
