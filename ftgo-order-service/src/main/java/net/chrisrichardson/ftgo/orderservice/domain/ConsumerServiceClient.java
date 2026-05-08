package net.chrisrichardson.ftgo.orderservice.domain;

import net.chrisrichardson.ftgo.common.Money;
import net.chrisrichardson.ftgo.consumerservice.api.web.ValidateOrderRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class ConsumerServiceClient implements ConsumerValidationService {

  private final RestTemplate restTemplate;
  private final String consumerServiceUrl;

  public ConsumerServiceClient(RestTemplate restTemplate,
                               @Value("${services.consumer.url:http://localhost:8082}") String consumerServiceUrl) {
    this.restTemplate = restTemplate;
    this.consumerServiceUrl = consumerServiceUrl;
  }

  @Override
  public void validateOrderForConsumer(long consumerId, Money orderTotal) {
    restTemplate.postForEntity(
            consumerServiceUrl + "/consumers/" + consumerId + "/validate",
            new ValidateOrderRequest(orderTotal),
            Void.class);
  }
}
