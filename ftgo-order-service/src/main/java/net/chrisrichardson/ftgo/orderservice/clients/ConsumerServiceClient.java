package net.chrisrichardson.ftgo.orderservice.clients;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import net.chrisrichardson.ftgo.common.Money;
import net.chrisrichardson.ftgo.consumerservice.api.web.ValidateOrderForConsumerRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

public class ConsumerServiceClient {

  private static final Logger logger = LoggerFactory.getLogger(ConsumerServiceClient.class);

  private final RestTemplate restTemplate;
  private final String consumerServiceUrl;

  public ConsumerServiceClient(RestTemplate restTemplate,
                               @Value("${services.consumer.url}") String consumerServiceUrl) {
    this.restTemplate = restTemplate;
    this.consumerServiceUrl = consumerServiceUrl;
  }

  @CircuitBreaker(name = "consumerService", fallbackMethod = "validateFallback")
  @Retry(name = "consumerService")
  public void validateOrderForConsumer(long consumerId, Money orderTotal) {
    String url = consumerServiceUrl + "/consumers/" + consumerId + "/validate";
    ValidateOrderForConsumerRequest request = new ValidateOrderForConsumerRequest(consumerId, orderTotal);
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    HttpEntity<ValidateOrderForConsumerRequest> entity = new HttpEntity<>(request, headers);
    ResponseEntity<Void> response = restTemplate.postForEntity(url, entity, Void.class);
    if (!response.getStatusCode().is2xxSuccessful()) {
      throw new ConsumerValidationFailedException("Consumer validation failed for consumerId: " + consumerId);
    }
  }

  @SuppressWarnings("unused")
  private void validateFallback(long consumerId, Money orderTotal, Exception e) {
    logger.error("Circuit breaker fallback: Consumer service unavailable for consumerId: {}", consumerId, e);
    throw new ConsumerValidationFailedException("Consumer service unavailable", e);
  }
}
