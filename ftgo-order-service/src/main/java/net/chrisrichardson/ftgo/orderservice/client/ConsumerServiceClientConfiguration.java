package net.chrisrichardson.ftgo.orderservice.client;

import net.chrisrichardson.ftgo.consumerservice.api.ConsumerService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Wires the {@link ConsumerService} integration seam to talk to the standalone
 * consumer microservice over HTTP. The target URL and timeouts are configurable
 * via properties with localhost defaults so the monolith works out of the box
 * against a locally running consumer service.
 */
@Configuration
public class ConsumerServiceClientConfiguration {

  @Bean
  public RestTemplate consumerServiceRestTemplate(
      RestTemplateBuilder builder,
      @Value("${consumer.service.connect-timeout-ms:5000}") int connectTimeoutMs,
      @Value("${consumer.service.read-timeout-ms:5000}") int readTimeoutMs) {
    return builder
        .setConnectTimeout(connectTimeoutMs)
        .setReadTimeout(readTimeoutMs)
        .build();
  }

  @Bean
  public ConsumerService consumerService(
      RestTemplate consumerServiceRestTemplate,
      @Value("${consumer.service.url:http://localhost:8082}") String consumerServiceUrl) {
    return new ConsumerServiceProxy(consumerServiceUrl, consumerServiceRestTemplate);
  }
}
