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

    @Value("${restaurant.service.connect-timeout:5000}")
    private int connectTimeout;

    @Value("${restaurant.service.read-timeout:5000}")
    private int readTimeout;

    @Bean
    public RestTemplate restaurantServiceRestTemplate(RestTemplateBuilder builder) {
        return builder
                .setConnectTimeout(connectTimeout)
                .setReadTimeout(readTimeout)
                .build();
    }

    @Bean
    public RestaurantServiceProxy restaurantServiceProxy(RestTemplate restaurantServiceRestTemplate) {
        return new RestaurantServiceProxy(restaurantServiceRestTemplate, restaurantServiceUrl);
    }
}
