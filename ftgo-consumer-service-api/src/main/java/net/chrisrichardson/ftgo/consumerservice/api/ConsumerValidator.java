package net.chrisrichardson.ftgo.consumerservice.api;

import net.chrisrichardson.ftgo.common.Money;

public interface ConsumerValidator {
  void validateOrderForConsumer(long consumerId, Money orderTotal);
}
