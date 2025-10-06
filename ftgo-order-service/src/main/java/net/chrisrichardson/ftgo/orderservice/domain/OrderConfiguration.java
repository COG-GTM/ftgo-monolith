package net.chrisrichardson.ftgo.orderservice.domain;

import io.micrometer.core.instrument.MeterRegistry;
import net.chrisrichardson.ftgo.domain.DomainConfiguration;
import net.chrisrichardson.ftgo.domain.OrderRepository;
import net.chrisrichardson.ftgo.orderservice.clients.ConsumerServiceClient;
import net.chrisrichardson.ftgo.orderservice.clients.CourierServiceClient;
import net.chrisrichardson.ftgo.orderservice.clients.RestaurantServiceClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

@Configuration
@Import({DomainConfiguration.class, HttpClientConfiguration.class})
public class OrderConfiguration {

  @Bean
  public ConsumerServiceClient consumerServiceClient(RestTemplate restTemplate,
                                                     @Value("${consumer.service.url:http://localhost:8081}") String url) {
    return new ConsumerServiceClient(restTemplate, url);
  }

  @Bean
  public RestaurantServiceClient restaurantServiceClient(RestTemplate restTemplate,
                                                         @Value("${restaurant.service.url:http://localhost:8082}") String url) {
    return new RestaurantServiceClient(restTemplate, url);
  }

  @Bean
  public CourierServiceClient courierServiceClient(RestTemplate restTemplate,
                                                   @Value("${courier.service.url:http://localhost:8083}") String url) {
    return new CourierServiceClient(restTemplate, url);
  }

  @Bean
  public OrderService orderService(OrderRepository orderRepository,
                                   Optional<MeterRegistry> meterRegistry,
                                   ConsumerServiceClient consumerServiceClient,
                                   RestaurantServiceClient restaurantServiceClient,
                                   CourierServiceClient courierServiceClient) {
    return new OrderService(orderRepository,
            restaurantServiceClient,
            meterRegistry,
            consumerServiceClient, courierServiceClient);
  }

  @Bean
  public MeterRegistryCustomizer meterRegistryCustomizer(@Value("${spring.application.name}") String serviceName) {
    return registry -> registry.config().commonTags("service", serviceName);
  }
}
