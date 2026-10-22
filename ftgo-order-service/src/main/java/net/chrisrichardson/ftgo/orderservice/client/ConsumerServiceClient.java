package net.chrisrichardson.ftgo.orderservice.client;

import net.chrisrichardson.ftgo.common.Money;
import net.chrisrichardson.ftgo.consumerservice.api.web.ValidateOrderRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/**
 * REST client that invokes the (now standalone) consumer service to validate an
 * order on behalf of a consumer. It mirrors the contract of
 * {@code ConsumerService.validateOrderForConsumer(long, Money)} but over HTTP.
 */
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
    try {
      restTemplate.postForEntity(url, new ValidateOrderRequest(orderTotal.asString()), Void.class);
    } catch (HttpClientErrorException e) {
      logger.warn("Consumer service rejected validation for consumer {}: {}", consumerId, e.getStatusCode());
      throw new ConsumerValidationFailedException(consumerId, e);
    } catch (RestClientException e) {
      logger.error("Failed to call consumer service at {}", url, e);
      throw new ConsumerValidationFailedException(consumerId, e);
    }
  }
}
