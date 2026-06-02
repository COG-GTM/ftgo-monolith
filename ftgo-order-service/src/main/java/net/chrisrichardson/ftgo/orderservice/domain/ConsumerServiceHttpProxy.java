package net.chrisrichardson.ftgo.orderservice.domain;

import net.chrisrichardson.ftgo.common.Money;
import net.chrisrichardson.ftgo.consumerservice.api.ConsumerNotFoundException;
import net.chrisrichardson.ftgo.consumerservice.api.ConsumerServiceProxy;
import net.chrisrichardson.ftgo.consumerservice.api.ConsumerVerificationFailedException;
import net.chrisrichardson.ftgo.consumerservice.api.web.ValidateOrderByConsumerRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

public class ConsumerServiceHttpProxy implements ConsumerServiceProxy {

  private final String baseUrl;
  private final RestTemplate restTemplate;

  public ConsumerServiceHttpProxy(String baseUrl, RestTemplate restTemplate) {
    this.baseUrl = baseUrl;
    this.restTemplate = restTemplate;
  }

  @Override
  public void validateOrderForConsumer(long consumerId, Money orderTotal) {
    URI uri = UriComponentsBuilder.fromHttpUrl(baseUrl)
            .path("/consumers/{consumerId}/validate")
            .buildAndExpand(consumerId)
            .toUri();
    try {
      restTemplate.postForEntity(uri, new ValidateOrderByConsumerRequest(orderTotal), Void.class);
    } catch (HttpClientErrorException e) {
      if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
        throw new ConsumerNotFoundException("Consumer not found: " + consumerId);
      }
      throw new ConsumerVerificationFailedException(
              "Order validation failed for consumer " + consumerId + ": " + e.getStatusCode());
    }
  }
}
