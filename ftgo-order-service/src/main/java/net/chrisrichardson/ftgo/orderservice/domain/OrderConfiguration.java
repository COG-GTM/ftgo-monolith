package net.chrisrichardson.ftgo.orderservice.domain;

import io.micrometer.core.instrument.MeterRegistry;
import net.chrisrichardson.ftgo.consumerservice.domain.ConsumerService;
import net.chrisrichardson.ftgo.domain.*;
import net.chrisrichardson.ftgo.orderservice.client.RestaurantServiceProxy;
import net.chrisrichardson.ftgo.orderservice.client.RestaurantServiceProxyConfiguration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.util.Optional;

@Configuration
@Import({DomainConfiguration.class, RestaurantServiceProxyConfiguration.class})
public class OrderConfiguration {

  @Bean
  public CourierAssignmentStrategy courierAssignmentStrategy() {
    return new DistanceOptimizedCourierAssignmentStrategy();
  }

  @Bean
  public OrderService orderService(RestaurantServiceProxy restaurantServiceProxy,
                                   OrderRepository orderRepository,
                                   Optional<MeterRegistry> meterRegistry,
                                   ConsumerService consumerService,
                                   CourierRepository courierRepository,
                                   CourierAssignmentStrategy courierAssignmentStrategy) {
    return new OrderService(orderRepository,
            restaurantServiceProxy,
            meterRegistry,
            consumerService,
            courierRepository,
            courierAssignmentStrategy);
  }

  @Bean
  public MeterRegistryCustomizer meterRegistryCustomizer(@Value("${spring.application.name}") String serviceName) {
    return registry -> registry.config().commonTags("service", serviceName);
  }
}
