package net.chrisrichardson.ftgo.orderservice.client;

import net.chrisrichardson.ftgo.common.Money;
import net.chrisrichardson.ftgo.consumerservice.api.ConsumerNotFoundException;
import net.chrisrichardson.ftgo.consumerservice.api.ConsumerVerificationFailedException;
import net.chrisrichardson.ftgo.consumerservice.api.web.ValidateOrderForConsumerRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

public class ConsumerServiceProxy {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final RestTemplate restTemplate;
    private final String consumerServiceUrl;

    public ConsumerServiceProxy(RestTemplate restTemplate, String consumerServiceUrl) {
        this.restTemplate = restTemplate;
        this.consumerServiceUrl = consumerServiceUrl;
    }

    public void validateOrderForConsumer(long consumerId, Money orderTotal) {
        String url = consumerServiceUrl + "/consumers/" + consumerId + "/validate";
        logger.debug("Validating order for consumer {} with total {} at {}", consumerId, orderTotal, url);

        ValidateOrderForConsumerRequest request = new ValidateOrderForConsumerRequest(orderTotal);
        HttpEntity<ValidateOrderForConsumerRequest> entity = new HttpEntity<>(request);

        try {
            ResponseEntity<Void> response = restTemplate.postForEntity(url, entity, Void.class);
            if (response.getStatusCode() == HttpStatus.OK) {
                logger.debug("Consumer {} validated successfully", consumerId);
                return;
            }
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                logger.warn("Consumer {} not found", consumerId);
                throw new ConsumerNotFoundException();
            } else if (e.getStatusCode() == HttpStatus.UNPROCESSABLE_ENTITY) {
                logger.warn("Consumer {} verification failed", consumerId);
                throw new ConsumerVerificationFailedException();
            }
            logger.error("Error validating consumer {}: {}", consumerId, e.getMessage());
            throw new RuntimeException("Error validating consumer: " + e.getMessage(), e);
        }
    }
}
