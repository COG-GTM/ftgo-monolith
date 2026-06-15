package net.chrisrichardson.ftgo.orderservice.client;

import net.chrisrichardson.ftgo.consumerservice.api.ConsumerService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class ConsumerServiceProxyConfiguration {

  @Bean
  public ConsumerService consumerService(
      RestTemplateBuilder restTemplateBuilder,
      @Value("${consumer.service.url:http://localhost:8082}") String consumerServiceUrl,
      @Value("${consumer.service.connect-timeout-millis:5000}") int connectTimeout,
      @Value("${consumer.service.read-timeout-millis:5000}") int readTimeout) {
    RestTemplate restTemplate = restTemplateBuilder
        .setConnectTimeout(connectTimeout)
        .setReadTimeout(readTimeout)
        .build();
    return new ConsumerServiceProxy(restTemplate, consumerServiceUrl);
  }
}
