package net.chrisrichardson.ftgo.orderservice.domain;

import net.chrisrichardson.ftgo.common.Money;
import net.chrisrichardson.ftgo.consumerservice.api.web.ValidateOrderRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

public class ConsumerServiceClient {

  private final Logger logger = LoggerFactory.getLogger(getClass());

  private final RestTemplate restTemplate;
  private final String consumerServiceUrl;

  public ConsumerServiceClient(RestTemplate restTemplate, String consumerServiceUrl) {
    this.restTemplate = restTemplate;
    this.consumerServiceUrl = consumerServiceUrl;
  }

  public void validateOrderForConsumer(long consumerId, Money orderTotal) {
    String url = consumerServiceUrl + "/consumers/" + consumerId + "/validate";
    
    ValidateOrderRequest request = new ValidateOrderRequest(orderTotal);
    
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    HttpEntity<ValidateOrderRequest> entity = new HttpEntity<>(request, headers);
    
    try {
      ResponseEntity<Void> response = restTemplate.postForEntity(url, entity, Void.class);
      if (!response.getStatusCode().is2xxSuccessful()) {
        logger.error("Consumer validation failed for consumerId={}, status={}", consumerId, response.getStatusCode());
        throw new ConsumerValidationException("Consumer validation failed");
      }
    } catch (HttpClientErrorException e) {
      logger.error("Consumer validation failed for consumerId={}, status={}", consumerId, e.getStatusCode());
      if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
        throw new ConsumerValidationException("Consumer not found: " + consumerId);
      }
      throw new ConsumerValidationException("Consumer validation failed: " + e.getMessage());
    }
  }
}
