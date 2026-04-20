package net.chrisrichardson.ftgo.orderservice.domain.proxy;

import net.chrisrichardson.ftgo.common.Money;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

public class ConsumerServiceProxyHttpClient implements ConsumerServiceProxy {

  private final Logger logger = LoggerFactory.getLogger(getClass());

  private final RestTemplate restTemplate;
  private final String consumerServiceUrl;

  public ConsumerServiceProxyHttpClient(RestTemplate restTemplate, String consumerServiceUrl) {
    this.restTemplate = restTemplate;
    this.consumerServiceUrl = consumerServiceUrl;
  }

  @Override
  public void validateOrderForConsumer(long consumerId, Money orderTotal) {
    String url = consumerServiceUrl + "/consumers/" + consumerId + "/validate-order";

    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put("orderTotal", orderTotal);

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

    try {
      logger.debug("Validating order for consumer {} with total {}", consumerId, orderTotal);
      ResponseEntity<Void> response = restTemplate.postForEntity(url, entity, Void.class);
      if (response.getStatusCode() != HttpStatus.OK) {
        throw new ConsumerVerificationFailedException(
                "Consumer validation failed for consumer " + consumerId);
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
