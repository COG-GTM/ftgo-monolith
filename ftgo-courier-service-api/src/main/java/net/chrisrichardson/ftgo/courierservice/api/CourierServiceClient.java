package net.chrisrichardson.ftgo.courierservice.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;

public class CourierServiceClient {

  private final Logger logger = LoggerFactory.getLogger(getClass());

  private static final int MAX_RETRIES = 3;
  private static final long INITIAL_BACKOFF_MS = 500;

  private final RestTemplate restTemplate;
  private final String courierServiceUrl;

  public CourierServiceClient(RestTemplate restTemplate, String courierServiceUrl) {
    this.restTemplate = restTemplate;
    this.courierServiceUrl = courierServiceUrl;
  }

  public long assignDelivery(long orderId, LocalDateTime readyBy) {
    ScheduleDeliveryRequest request = new ScheduleDeliveryRequest(orderId, readyBy);
    ScheduleDeliveryResponse response = executeWithRetry(() ->
            restTemplate.postForObject(
                    courierServiceUrl + "/couriers/schedule-delivery",
                    request,
                    ScheduleDeliveryResponse.class), "assignDelivery");
    return response.getCourierId();
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
