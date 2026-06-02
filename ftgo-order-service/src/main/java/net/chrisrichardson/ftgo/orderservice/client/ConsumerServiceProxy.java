package net.chrisrichardson.ftgo.orderservice.client;

import net.chrisrichardson.ftgo.common.Money;
import net.chrisrichardson.ftgo.consumerservice.api.web.ConsumerNotFoundException;
import net.chrisrichardson.ftgo.consumerservice.api.web.ConsumerVerificationFailedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/**
 * HTTP client proxy that lets the order service validate consumers by calling the
 * extracted consumer service over REST instead of invoking it in-process.
 */
public class ConsumerServiceProxy {

  private final Logger logger = LoggerFactory.getLogger(getClass());

  private final RestTemplate restTemplate;
  private final String consumerServiceUrl;

  public ConsumerServiceProxy(RestTemplate restTemplate, String consumerServiceUrl) {
    this.restTemplate = restTemplate;
    this.consumerServiceUrl = consumerServiceUrl.endsWith("/")
            ? consumerServiceUrl.substring(0, consumerServiceUrl.length() - 1)
            : consumerServiceUrl;
  }

  /**
   * Validates that the consumer exists by issuing a GET to the consumer service.
   * {@code validateOrderForConsumer} returns no data, so existence (HTTP 200) is
   * sufficient to consider the consumer valid.
   */
  public void validateOrderForConsumer(long consumerId, Money orderTotal) {
    String url = consumerServiceUrl + "/consumers/" + consumerId;
    logger.debug("Validating consumer {} (orderTotal={}) via {}", consumerId, orderTotal, url);
    try {
      restTemplate.getForEntity(url, String.class);
      logger.debug("Consumer {} validated successfully", consumerId);
    } catch (HttpClientErrorException e) {
      if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
        logger.warn("Consumer {} not found (HTTP 404) from consumer service", consumerId);
        throw new ConsumerNotFoundException();
      }
      logger.error("Consumer service returned {} for consumer {}", e.getStatusCode(), consumerId, e);
      throw new ConsumerVerificationFailedException(
              "Consumer service returned " + e.getStatusCode() + " for consumer " + consumerId, e);
    } catch (RestClientException e) {
      logger.error("Failed to call consumer service at {} for consumer {}", url, consumerId, e);
      throw new ConsumerVerificationFailedException(
              "Failed to call consumer service for consumer " + consumerId, e);
    }
  }
}
