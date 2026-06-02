package net.chrisrichardson.ftgo;

import net.chrisrichardson.ftgo.consumerservice.domain.ConsumerService;
import net.chrisrichardson.ftgo.orderservice.client.ConsumerServiceClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * In the monolith the consumer service is co-located with the order service, so order
 * creation validates consumers in-process — joining the caller's transaction, exactly as
 * before the extraction.
 *
 * <p>This deliberately avoids routing the validation through {@code ConsumerServiceProxy}
 * (HTTP). A self-call over HTTP would (a) require a second server port, and (b) hold the
 * {@code createOrder} transaction's DB connection open across an HTTP round-trip that
 * itself needs a connection to serve {@code GET /consumers/{id}} — a connection-pool
 * deadlock under concurrency. The HTTP proxy bean remains defined for the standalone
 * order-service deployment; this {@link Primary} bean takes precedence inside the monolith.
 */
@Configuration
public class MonolithConsumerServiceConfiguration {

  @Bean
  @Primary
  public ConsumerServiceClient inProcessConsumerServiceClient(ConsumerService consumerService) {
    return consumerService::validateOrderForConsumer;
  }
}
