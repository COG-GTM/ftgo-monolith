package net.chrisrichardson.ftgo.consumerservice.api;

import net.chrisrichardson.ftgo.common.Money;

public interface ConsumerVerificationService {

  /**
   * @throws ConsumerNotFoundException if no consumer exists with the given id
   * @throws ConsumerVerificationFailedException if the consumer may not place the order
   */
  void validateOrderForConsumer(long consumerId, Money orderTotal);
}
