package net.chrisrichardson.ftgo.orderservice.domain.proxy;

import net.chrisrichardson.ftgo.common.Money;

public interface ConsumerValidationService {

  void validateOrderForConsumer(long consumerId, Money orderTotal);
}
