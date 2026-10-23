package net.chrisrichardson.ftgo.orderservice.client;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestaurantServiceProxyConfiguration {

  @Value("${restaurant.service.url:http://localhost:8083}")
  private String restaurantServiceUrl;

  @Value("${restaurant.service.connect-timeout-millis:5000}")
  private int connectTimeoutMillis;

  @Value("${restaurant.service.read-timeout-millis:5000}")
  private int readTimeoutMillis;

  @Bean
  public RestTemplate restaurantServiceRestTemplate(RestTemplateBuilder builder) {
    return builder
            .setConnectTimeout(connectTimeoutMillis)
            .setReadTimeout(readTimeoutMillis)
            .build();
  }

  @Bean
  public RestaurantServiceProxy restaurantServiceProxy(
          @Qualifier("restaurantServiceRestTemplate") RestTemplate restaurantServiceRestTemplate) {
    return new RestaurantServiceProxy(restaurantServiceRestTemplate, restaurantServiceUrl);
  }
}
