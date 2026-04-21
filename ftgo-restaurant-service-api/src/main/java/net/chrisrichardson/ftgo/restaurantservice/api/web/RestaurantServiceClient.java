package net.chrisrichardson.ftgo.restaurantservice.api.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

public class RestaurantServiceClient {

  private final Logger logger = LoggerFactory.getLogger(getClass());

  private static final int MAX_RETRIES = 3;
  private static final long INITIAL_BACKOFF_MS = 500;

  private final RestTemplate restTemplate;
  private final String restaurantServiceUrl;

  public RestaurantServiceClient(RestTemplate restTemplate, String restaurantServiceUrl) {
    this.restTemplate = restTemplate;
    this.restaurantServiceUrl = restaurantServiceUrl;
  }

  public GetRestaurantResponse findById(long restaurantId) {
    return executeWithRetry(() -> {
      try {
        return restTemplate.getForObject(
                restaurantServiceUrl + "/restaurants/{restaurantId}",
                GetRestaurantResponse.class,
                restaurantId);
      } catch (HttpClientErrorException e) {
        if (e.getStatusCode().value() == 404) {
          return null;
        }
        throw e;
      }
    }, "findRestaurantById");
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
