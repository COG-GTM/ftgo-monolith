package net.chrisrichardson.ftgo.orderservice.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestaurantServiceProxyConfiguration {

  @Value("${restaurant.service.url:http://localhost:8082}")
  private String restaurantServiceUrl;

  @Value("${restaurant.service.connect-timeout-ms:5000}")
  private int connectTimeoutMs;

  @Value("${restaurant.service.read-timeout-ms:5000}")
  private int readTimeoutMs;

  @Bean
  public RestTemplate restaurantServiceRestTemplate(RestTemplateBuilder builder) {
    return builder
            .setConnectTimeout(connectTimeoutMs)
            .setReadTimeout(readTimeoutMs)
            .build();
  }

  @Bean
  public RestaurantServiceProxy restaurantServiceProxy(RestTemplate restaurantServiceRestTemplate) {
    return new RestaurantServiceProxy(restaurantServiceUrl, restaurantServiceRestTemplate);
  }
}
