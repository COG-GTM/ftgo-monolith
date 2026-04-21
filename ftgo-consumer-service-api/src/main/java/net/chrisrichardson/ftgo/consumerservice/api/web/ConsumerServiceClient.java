package net.chrisrichardson.ftgo.consumerservice.api.web;

import net.chrisrichardson.ftgo.common.Money;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

public class ConsumerServiceClient {

  private final Logger logger = LoggerFactory.getLogger(getClass());

  private static final int MAX_RETRIES = 3;
  private static final long INITIAL_BACKOFF_MS = 500;

  private final RestTemplate restTemplate;
  private final String consumerServiceUrl;

  public ConsumerServiceClient(RestTemplate restTemplate, String consumerServiceUrl) {
    this.restTemplate = restTemplate;
    this.consumerServiceUrl = consumerServiceUrl;
  }

  public void validateOrderForConsumer(long consumerId, Money orderTotal) {
    ValidateConsumerRequest request = new ValidateConsumerRequest(orderTotal);
    executeWithRetry(() -> {
      restTemplate.postForEntity(
              consumerServiceUrl + "/consumers/{consumerId}/validate",
              request,
              Void.class,
              consumerId);
      return null;
    }, "validateOrderForConsumer");
  }

  public CreateConsumerResponse createConsumer(CreateConsumerRequest request) {
    return executeWithRetry(() -> restTemplate.postForObject(
            consumerServiceUrl + "/consumers",
            request,
            CreateConsumerResponse.class), "createConsumer");
  }

  private <T> T executeWithRetry(RetryableOperation<T> operation, String operationName) {
    int attempt = 0;
    while (true) {
      try {
        return operation.execute();
      } catch (HttpClientErrorException e) {
        logger.error("{} failed with client error: {}", operationName, e.getMessage());
        throw new RuntimeException(operationName + " failed", e);
      } catch (ResourceAccessException e) {
        attempt++;
        if (attempt >= MAX_RETRIES) {
          logger.error("{} failed after {} retries: {}", operationName, MAX_RETRIES, e.getMessage());
          throw new RuntimeException(operationName + " failed after retries", e);
        }
        long backoff = INITIAL_BACKOFF_MS * (1L << (attempt - 1));
        logger.warn("{} attempt {} failed, retrying in {}ms: {}", operationName, attempt, backoff, e.getMessage());
        try {
          Thread.sleep(backoff);
        } catch (InterruptedException ie) {
          Thread.currentThread().interrupt();
          throw new RuntimeException(operationName + " interrupted during retry", ie);
        }
      }
    }
  }

  @FunctionalInterface
  private interface RetryableOperation<T> {
    T execute();
  }
}
