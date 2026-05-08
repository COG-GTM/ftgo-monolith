package net.chrisrichardson.ftgo.orderservice.domain;

import net.chrisrichardson.ftgo.common.Money;

public interface ConsumerValidation {
  void validateOrderForConsumer(long consumerId, Money orderTotal);
}
