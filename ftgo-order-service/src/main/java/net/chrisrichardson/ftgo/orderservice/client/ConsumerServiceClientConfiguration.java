package net.chrisrichardson.ftgo.orderservice.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class ConsumerServiceClientConfiguration {

    @Value("${consumer.service.url:http://localhost:8082}")
    private String consumerServiceUrl;

    @Value("${consumer.service.connect-timeout:5000}")
    private int connectTimeout;

    @Value("${consumer.service.read-timeout:5000}")
    private int readTimeout;

    @Bean
    public RestTemplate consumerServiceRestTemplate(RestTemplateBuilder builder) {
        return builder
                .setConnectTimeout(connectTimeout)
                .setReadTimeout(readTimeout)
                .build();
    }

    @Bean
    public ConsumerServiceProxy consumerServiceProxy(RestTemplate consumerServiceRestTemplate) {
        return new ConsumerServiceProxy(consumerServiceRestTemplate, consumerServiceUrl);
    }
}
