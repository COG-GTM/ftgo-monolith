package net.chrisrichardson.ftgo.orderservice.client;

import net.chrisrichardson.ftgo.common.Money;
import net.chrisrichardson.ftgo.consumerservice.api.ConsumerNotFoundException;
import net.chrisrichardson.ftgo.consumerservice.api.ConsumerService;
import net.chrisrichardson.ftgo.consumerservice.api.ConsumerVerificationFailedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/**
 * HTTP client implementation of the {@link ConsumerService} contract.
 *
 * <p>Replaces the previous in-process call into the consumer module with a REST
 * call against the standalone consumer microservice. The original
 * {@code validateOrderForConsumer} returns no data, so existence/eligibility is
 * verified by issuing a {@code GET /consumers/{id}} and translating the HTTP
 * status into the same domain exceptions callers already expect.
 */
public class ConsumerServiceProxy implements ConsumerService {

  private static final Logger logger = LoggerFactory.getLogger(ConsumerServiceProxy.class);

  private final String baseUrl;
  private final RestTemplate restTemplate;

  public ConsumerServiceProxy(String baseUrl, RestTemplate restTemplate) {
    this.baseUrl = baseUrl;
    this.restTemplate = restTemplate;
  }

  @Override
  public void validateOrderForConsumer(long consumerId, Money orderTotal) {
    String url = baseUrl + "/consumers/" + consumerId;
    logger.debug("Validating order for consumer {} via {}", consumerId, url);
    try {
      restTemplate.getForEntity(url, String.class);
    } catch (HttpClientErrorException e) {
      if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
        logger.warn("Consumer {} not found at {}", consumerId, url);
        throw new ConsumerNotFoundException("Consumer not found with id " + consumerId);
      }
      throw new ConsumerVerificationFailedException(
          "Consumer verification failed for id " + consumerId + ": " + e.getStatusCode(), e);
    } catch (RestClientException e) {
      logger.error("Unable to reach consumer service at {}: {}", url, e.getMessage());
      throw new ConsumerVerificationFailedException(
          "Unable to reach consumer service at " + url, e);
    }
  }
}
