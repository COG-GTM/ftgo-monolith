package net.chrisrichardson.ftgo.orderservice.domain;

import net.chrisrichardson.ftgo.common.Money;

public interface ConsumerServiceProxy {
  void validateOrderForConsumer(long consumerId, Money orderTotal);
}
