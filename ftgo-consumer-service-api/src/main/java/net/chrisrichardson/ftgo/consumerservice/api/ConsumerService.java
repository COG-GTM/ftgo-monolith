package net.chrisrichardson.ftgo.consumerservice.api;

import net.chrisrichardson.ftgo.common.Money;

/**
 * The Consumer bounded context's published contract.
 *
 * <p>This is the API seam between the monolith and the extracted Consumer
 * microservice. Callers (e.g. the Order service) depend on this interface
 * rather than on a concrete in-process implementation, so the collaboration can
 * be satisfied either locally or over HTTP without the caller knowing which.
 */
public interface ConsumerService {

  /**
   * Verifies that the consumer exists and is allowed to place an order of the
   * given total. Throws {@link ConsumerNotFoundException} if the consumer does
   * not exist and {@link ConsumerVerificationFailedException} if verification
   * cannot be completed.
   */
  void validateOrderForConsumer(long consumerId, Money orderTotal);
}
