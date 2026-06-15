package net.chrisrichardson.ftgo.orderservice.client;

import net.chrisrichardson.ftgo.common.Money;
import net.chrisrichardson.ftgo.consumerservice.api.ConsumerNotFoundException;
import net.chrisrichardson.ftgo.consumerservice.api.ConsumerService;
import net.chrisrichardson.ftgo.consumerservice.api.ConsumerVerificationFailedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

/**
 * HTTP client that lets the Order service collaborate with the extracted
 * Consumer microservice while still depending only on the
 * {@link ConsumerService} interface.
 *
 * <p>It preserves the original in-process behavior: a missing consumer surfaces
 * as a {@link ConsumerNotFoundException}, and any other failure to verify the
 * consumer surfaces as a {@link ConsumerVerificationFailedException}. Because
 * verification happens before the order is persisted, throwing here still aborts
 * {@code OrderService.createOrder} before {@code orderRepository.save}, keeping
 * the surrounding {@code @Transactional} rollback semantics unchanged.
 */
public class ConsumerServiceProxy implements ConsumerService {

  private static final Logger logger = LoggerFactory.getLogger(ConsumerServiceProxy.class);

  private final RestTemplate restTemplate;
  private final String consumerServiceBaseUrl;

  public ConsumerServiceProxy(RestTemplate restTemplate, String consumerServiceBaseUrl) {
    this.restTemplate = restTemplate;
    this.consumerServiceBaseUrl = consumerServiceBaseUrl;
  }

  @Override
  public void validateOrderForConsumer(long consumerId, Money orderTotal) {
    String url = consumerServiceBaseUrl + "/consumers/" + consumerId;
    logger.debug("Validating order for consumer {} via {}", consumerId, url);
    try {
      restTemplate.getForEntity(url, String.class);
    } catch (HttpStatusCodeException e) {
      if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
        logger.warn("Consumer {} not found", consumerId);
        throw new ConsumerNotFoundException("Consumer " + consumerId + " not found");
      }
      logger.error("Consumer service returned {} validating consumer {}", e.getStatusCode(), consumerId);
      throw new ConsumerVerificationFailedException(
          "Consumer service returned " + e.getStatusCode() + " validating consumer " + consumerId, e);
    } catch (ResourceAccessException e) {
      logger.error("Unable to reach consumer service at {}: {}", url, e.getMessage());
      throw new ConsumerVerificationFailedException(
          "Unable to reach consumer service at " + url, e);
    }
  }
}
