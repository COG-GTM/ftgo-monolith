package net.chrisrichardson.ftgo.orderservice.domain;

import io.micrometer.core.instrument.MeterRegistry;
import net.chrisrichardson.ftgo.domain.DomainConfiguration;
import net.chrisrichardson.ftgo.domain.OrderRepository;
import net.chrisrichardson.ftgo.orderservice.client.ConsumerServiceClient;
import net.chrisrichardson.ftgo.orderservice.client.CourierServiceClient;
import net.chrisrichardson.ftgo.orderservice.client.RestaurantServiceClient;
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
  public OrderService orderService(OrderRepository orderRepository,
                                   Optional<MeterRegistry> meterRegistry,
                                   ConsumerServiceClient consumerServiceClient,
                                   RestaurantServiceClient restaurantServiceClient,
                                   CourierServiceClient courierServiceClient) {
    return new OrderService(orderRepository,
            restaurantServiceClient,
            meterRegistry,
            consumerServiceClient, 
            courierServiceClient);
  }

  @Bean
  public MeterRegistryCustomizer meterRegistryCustomizer(@Value("${spring.application.name}") String serviceName) {
    return registry -> registry.config().commonTags("service", serviceName);
  }
}
