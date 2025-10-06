package net.chrisrichardson.ftgo.orderservice.clients;

import net.chrisrichardson.ftgo.common.Money;
import net.chrisrichardson.ftgo.consumerservice.api.web.ValidateOrderRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Service
public class ConsumerServiceClient {
    private final WebClient webClient;
    
    public ConsumerServiceClient(@Value("${consumer.service.url}") String baseUrl) {
        this.webClient = WebClient.builder().baseUrl(baseUrl).build();
    }
    
    public void validateOrderForConsumer(long consumerId, Money orderTotal) {
        try {
            webClient.post()
                .uri("/consumers/{consumerId}/validate", consumerId)
                .body(BodyInserters.fromObject(new ValidateOrderRequest(orderTotal)))
                .retrieve()
                .bodyToMono(Void.class)
                .block();
        } catch (WebClientResponseException e) {
            if (e.getRawStatusCode() == 404) {
                throw new RuntimeException("Consumer not found: " + consumerId, e);
            }
            throw e;
        }
    }
}
