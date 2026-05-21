package net.chrisrichardson.ftgo.orderservice.domain;

import net.chrisrichardson.ftgo.common.Money;
import org.springframework.web.client.RestTemplate;

public class ConsumerServiceProxy implements ConsumerServiceClient {
    private final RestTemplate restTemplate;
    private final String consumerServiceUrl;

    public ConsumerServiceProxy(RestTemplate restTemplate, String consumerServiceUrl) {
        this.restTemplate = restTemplate;
        this.consumerServiceUrl = consumerServiceUrl;
    }

    @Override
    public void validateOrderForConsumer(long consumerId, Money orderTotal) {
        String url = consumerServiceUrl + "/consumers/" + consumerId + "/validate";
        restTemplate.postForEntity(url, new ValidateOrderRequest(orderTotal), Void.class);
    }
}
