package net.chrisrichardson.ftgo.orderservice.domain.proxy;

import net.chrisrichardson.ftgo.common.Money;
import net.chrisrichardson.ftgo.consumerservice.api.web.ValidateOrderForConsumerRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

public class ConsumerServiceHttpProxy implements ConsumerValidationService {

  private final Logger logger = LoggerFactory.getLogger(getClass());

  private final RestTemplate restTemplate;
  private final String consumerServiceUrl;

  public ConsumerServiceHttpProxy(RestTemplate restTemplate, String consumerServiceUrl) {
    this.restTemplate = restTemplate;
    this.consumerServiceUrl = consumerServiceUrl;
  }

  @Override
  public void validateOrderForConsumer(long consumerId, Money orderTotal) {
    String url = consumerServiceUrl + "/consumers/" + consumerId + "/validate";
    logger.info("Validating order for consumer {} via {}", consumerId, url);

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);

    ValidateOrderForConsumerRequest request = new ValidateOrderForConsumerRequest(orderTotal);
    HttpEntity<ValidateOrderForConsumerRequest> entity = new HttpEntity<>(request, headers);

    try {
      ResponseEntity<Void> response = restTemplate.postForEntity(url, entity, Void.class);
      if (response.getStatusCode() != HttpStatus.OK) {
        throw new ConsumerVerificationFailedException(
                "Consumer validation failed for consumer " + consumerId + ": status " + response.getStatusCode());
      }
    } catch (HttpClientErrorException e) {
      if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
        throw new ConsumerNotFoundException("Consumer not found: " + consumerId);
      }
      throw new ConsumerVerificationFailedException(
              "Consumer validation failed for consumer " + consumerId + ": " + e.getMessage());
    }
  }
}
