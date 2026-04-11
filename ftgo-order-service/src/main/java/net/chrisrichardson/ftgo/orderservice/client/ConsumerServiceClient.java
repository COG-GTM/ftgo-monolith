package net.chrisrichardson.ftgo.orderservice.client;

import net.chrisrichardson.ftgo.common.Money;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Component
public class ConsumerServiceClient {

  private final RestTemplate restTemplate;
  private final String consumerServiceUrl;

  public ConsumerServiceClient(RestTemplate restTemplate,
                                @Value("${consumer.service.url}") String consumerServiceUrl) {
    this.restTemplate = restTemplate;
    this.consumerServiceUrl = consumerServiceUrl;
  }

  public void validateOrderForConsumer(long consumerId, Money orderTotal) {
    String url = consumerServiceUrl + "/consumers/" + consumerId + "/validate";
    
    Map<String, Object> request = new HashMap<>();
    request.put("orderTotal", orderTotal);
    
    ResponseEntity<Void> response = restTemplate.postForEntity(url, request, Void.class);
    
    if (response.getStatusCode() != HttpStatus.OK) {
      throw new RuntimeException("Consumer validation failed for consumer: " + consumerId);
    }
  }
}
