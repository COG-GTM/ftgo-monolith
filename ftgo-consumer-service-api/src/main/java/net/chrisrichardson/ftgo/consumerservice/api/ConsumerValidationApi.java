package net.chrisrichardson.ftgo.consumerservice.api;

import net.chrisrichardson.ftgo.common.Money;

public interface ConsumerValidationApi {

  /**
   * @throws ConsumerNotFoundException if the consumer does not exist
   * @throws ConsumerVerificationFailedException if the consumer may not place the order
   */
  void validateOrderForConsumer(long consumerId, Money orderTotal);
}
