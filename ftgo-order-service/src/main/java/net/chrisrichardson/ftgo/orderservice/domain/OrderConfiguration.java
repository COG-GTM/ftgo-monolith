package net.chrisrichardson.ftgo.orderservice.domain;

import io.micrometer.core.instrument.MeterRegistry;
import net.chrisrichardson.ftgo.consumerservice.api.web.ConsumerServiceClient;
import net.chrisrichardson.ftgo.courierservice.api.CourierServiceClient;
import net.chrisrichardson.ftgo.domain.DomainConfiguration;
import net.chrisrichardson.ftgo.domain.OrderRepository;
import net.chrisrichardson.ftgo.restaurantservice.api.web.RestaurantServiceClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

@Configuration
@Import(DomainConfiguration.class)
public class OrderConfiguration {

  @Bean
  public RestTemplate restTemplate() {
    return new RestTemplate();
  }

  @Bean
  public ConsumerServiceClient consumerServiceClient(RestTemplate restTemplate,
                                                     @Value("${ftgo.services.consumer.url:http://localhost:8082}") String consumerServiceUrl) {
    return new ConsumerServiceClient(restTemplate, consumerServiceUrl);
  }

  @Bean
  public RestaurantServiceClient restaurantServiceClient(RestTemplate restTemplate,
                                                         @Value("${ftgo.services.restaurant.url:http://localhost:8083}") String restaurantServiceUrl) {
    return new RestaurantServiceClient(restTemplate, restaurantServiceUrl);
  }

  @Bean
  public CourierServiceClient courierServiceClient(RestTemplate restTemplate,
                                                   @Value("${ftgo.services.courier.url:http://localhost:8084}") String courierServiceUrl) {
    return new CourierServiceClient(restTemplate, courierServiceUrl);
  }

  @Bean
  public OrderService orderService(OrderRepository orderRepository,
                                   Optional<MeterRegistry> meterRegistry,
                                   ConsumerServiceClient consumerServiceClient,
                                   RestaurantServiceClient restaurantServiceClient,
                                   CourierServiceClient courierServiceClient) {
    return new OrderService(orderRepository,
            meterRegistry,
            consumerServiceClient,
            restaurantServiceClient,
            courierServiceClient);
  }

  @Bean
  public MeterRegistryCustomizer meterRegistryCustomizer(@Value("${spring.application.name}") String serviceName) {
    return registry -> registry.config().commonTags("service", serviceName);
  }
}
