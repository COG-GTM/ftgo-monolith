package net.chrisrichardson.ftgo.orderservice.client;

import net.chrisrichardson.ftgo.common.Money;
import net.chrisrichardson.ftgo.consumerservice.api.ConsumerNotFoundException;
import net.chrisrichardson.ftgo.consumerservice.api.ConsumerVerificationFailedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
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
        String url = consumerServiceUrl + "/consumers/" + consumerId;
        logger.info("Validating consumer {} via HTTP call to {}", consumerId, url);

        try {
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                logger.info("Consumer {} validated successfully", consumerId);
            } else {
                logger.warn("Unexpected response status {} for consumer {}", response.getStatusCode(), consumerId);
                throw new ConsumerVerificationFailedException();
            }
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                logger.warn("Consumer {} not found", consumerId);
                throw new ConsumerNotFoundException();
            }
            logger.error("HTTP error validating consumer {}: {}", consumerId, e.getMessage());
            throw new ConsumerVerificationFailedException();
        } catch (RestClientException e) {
            logger.error("Error calling Consumer Service for consumer {}: {}", consumerId, e.getMessage());
            throw new ConsumerVerificationFailedException();
        }
    }
}
