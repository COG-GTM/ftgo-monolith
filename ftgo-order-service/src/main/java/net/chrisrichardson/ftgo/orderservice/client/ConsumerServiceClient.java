package net.chrisrichardson.ftgo.orderservice.client;

import net.chrisrichardson.ftgo.common.Money;

/**
 * Abstraction the order service uses to validate consumers. Decouples {@code OrderService}
 * from how the consumer service is reached:
 * <ul>
 *   <li>when the order service is deployed standalone it is backed by {@link ConsumerServiceProxy},
 *       which calls the extracted consumer service over HTTP;</li>
 *   <li>inside the monolith an in-process adapter delegates directly to the co-located
 *       consumer service, preserving the original single-transaction behaviour.</li>
 * </ul>
 */
public interface ConsumerServiceClient {

  void validateOrderForConsumer(long consumerId, Money orderTotal);
}
