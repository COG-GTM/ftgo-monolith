package net.chrisrichardson.ftgo.orderservice.clients;

import net.chrisrichardson.ftgo.common.Money;
import net.chrisrichardson.ftgo.consumerservice.api.web.ValidateOrderRequest;
import net.chrisrichardson.ftgo.consumerservice.domain.ConsumerNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Service
public class ConsumerClient {
    private final RestTemplate restTemplate;
    private final String consumerServiceUrl;

    public ConsumerClient(RestTemplate restTemplate,
                         @Value("${consumer.service.url:http://localhost:8083}") String consumerServiceUrl) {
        this.restTemplate = restTemplate;
        this.consumerServiceUrl = consumerServiceUrl;
    }

    public void validateOrderForConsumer(long consumerId, Money orderTotal) {
        try {
            restTemplate.postForEntity(
                consumerServiceUrl + "/consumers/" + consumerId + "/validate",
                new ValidateOrderRequest(orderTotal),
                String.class
            );
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode().value() == 404) {
                throw new ConsumerNotFoundException();
            }
            throw e;
        }
    }
}
