package net.chrisrichardson.ftgo.orderservice.domain;

import net.chrisrichardson.ftgo.common.Money;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

public class HttpConsumerServiceProxy implements ConsumerServiceProxy {

  private final RestTemplate restTemplate;
  private final String consumerServiceUrl;

  public HttpConsumerServiceProxy(RestTemplate restTemplate, String consumerServiceUrl) {
    this.restTemplate = restTemplate;
    this.consumerServiceUrl = consumerServiceUrl;
  }

  @Override
  public void validateOrderForConsumer(long consumerId, Money orderTotal) {
    Map<String, Object> request = new HashMap<>();
    request.put("orderTotal", orderTotal.asString());

    restTemplate.postForEntity(
            consumerServiceUrl + "/consumers/{consumerId}/validate",
            request,
            Void.class,
            consumerId);
  }
}
