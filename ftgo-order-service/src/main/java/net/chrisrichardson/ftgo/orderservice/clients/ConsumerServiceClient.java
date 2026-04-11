package net.chrisrichardson.ftgo.orderservice.clients;

import net.chrisrichardson.ftgo.common.Money;
import net.chrisrichardson.ftgo.consumerservice.api.ValidateOrderRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

public class ConsumerServiceClient {

  private final RestTemplate restTemplate;
  private final String consumerServiceUrl;

  public ConsumerServiceClient(RestTemplate restTemplate,
                               @Value("${consumer.service.url:http://localhost:8081}") String consumerServiceUrl) {
    this.restTemplate = restTemplate;
    this.consumerServiceUrl = consumerServiceUrl;
  }

  public void validateOrderForConsumer(long consumerId, Money orderTotal) {
    String url = consumerServiceUrl + "/consumers/" + consumerId + "/validate-order";
    ValidateOrderRequest request = new ValidateOrderRequest(orderTotal);
    try {
      restTemplate.postForEntity(url, request, String.class);
    } catch (HttpClientErrorException e) {
      if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
        throw new ConsumerVerificationFailedException();
      }
      throw e;
    }
  }

  public static class ConsumerVerificationFailedException extends RuntimeException {
  }
}
