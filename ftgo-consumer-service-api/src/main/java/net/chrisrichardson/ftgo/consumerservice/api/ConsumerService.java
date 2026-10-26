package net.chrisrichardson.ftgo.consumerservice.api;

import net.chrisrichardson.ftgo.common.Money;

/**
 * Contract for verifying that a consumer is allowed to place an order.
 *
 * <p>This is the integration seam between the order bounded context and the
 * consumer bounded context. The monolith depends only on this interface; the
 * behaviour is provided either by the in-process consumer implementation or by
 * an HTTP proxy talking to the standalone consumer microservice.
 */
public interface ConsumerService {

  /**
   * Verifies that the given consumer exists and is permitted to place an order
   * for the supplied total.
   *
   * @param consumerId the id of the consumer placing the order
   * @param orderTotal the total amount of the order
   * @throws ConsumerNotFoundException if no consumer exists with the given id
   * @throws ConsumerVerificationFailedException if verification cannot be completed
   */
  void validateOrderForConsumer(long consumerId, Money orderTotal);
}
