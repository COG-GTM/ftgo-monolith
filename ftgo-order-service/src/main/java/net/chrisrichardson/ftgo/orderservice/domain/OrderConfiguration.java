package net.chrisrichardson.ftgo.orderservice.domain;

import io.micrometer.core.instrument.MeterRegistry;
import net.chrisrichardson.ftgo.domain.*;
import net.chrisrichardson.ftgo.orderservice.domain.proxy.ConsumerServiceHttpProxy;
import net.chrisrichardson.ftgo.orderservice.domain.proxy.ConsumerValidationService;
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
  public CourierAssignmentStrategy courierAssignmentStrategy() {
    return new DistanceOptimizedCourierAssignmentStrategy();
  }

  @Bean
  public RestTemplate consumerServiceRestTemplate() {
    return new RestTemplate();
  }

  @Bean
  public ConsumerValidationService consumerValidationService(
          RestTemplate consumerServiceRestTemplate,
          @Value("${consumer.service.url:http://localhost:8082}") String consumerServiceUrl) {
    return new ConsumerServiceHttpProxy(consumerServiceRestTemplate, consumerServiceUrl);
  }

  @Bean
  public OrderService orderService(RestaurantRepository restaurantRepository,
                                   OrderRepository orderRepository,
                                   Optional<MeterRegistry> meterRegistry,
                                   ConsumerValidationService consumerValidationService,
                                   CourierRepository courierRepository,
                                   CourierAssignmentStrategy courierAssignmentStrategy) {
    return new OrderService(orderRepository,
            restaurantRepository,
            meterRegistry,
            consumerValidationService,
            courierRepository,
            courierAssignmentStrategy);
  }

  @Bean
  public MeterRegistryCustomizer meterRegistryCustomizer(@Value("${spring.application.name}") String serviceName) {
    return registry -> registry.config().commonTags("service", serviceName);
  }
}
