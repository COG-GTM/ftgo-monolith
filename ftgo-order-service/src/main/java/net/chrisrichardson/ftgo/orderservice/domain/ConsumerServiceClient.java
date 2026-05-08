package net.chrisrichardson.ftgo.orderservice.domain;

import net.chrisrichardson.ftgo.common.Money;
import net.chrisrichardson.ftgo.consumerservice.api.web.ValidateOrderRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

public class ConsumerServiceClient implements ConsumerValidation {

  private final RestTemplate restTemplate;
  private final String consumerServiceUrl;

  public ConsumerServiceClient(RestTemplate restTemplate,
                               @Value("${services.consumer-service.url:http://consumer-service:8080}") String consumerServiceUrl) {
    this.restTemplate = restTemplate;
    this.consumerServiceUrl = consumerServiceUrl;
  }

  @Override
  public void validateOrderForConsumer(long consumerId, Money orderTotal) {
    String url = consumerServiceUrl + "/consumers/" + consumerId + "/validate";
    try {
      ResponseEntity<Void> response = restTemplate.postForEntity(url, new ValidateOrderRequest(orderTotal), Void.class);
      if (!response.getStatusCode().is2xxSuccessful()) {
        throw new ConsumerValidationFailedException("Consumer validation failed: " + response.getStatusCode());
      }
    } catch (HttpClientErrorException e) {
      if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
        throw new ConsumerValidationFailedException("Consumer not found: " + consumerId);
      } else if (e.getStatusCode() == HttpStatus.UNPROCESSABLE_ENTITY) {
        throw new ConsumerValidationFailedException("Consumer order validation failed for consumer: " + consumerId);
      }
      throw e;
    }
  }
}
