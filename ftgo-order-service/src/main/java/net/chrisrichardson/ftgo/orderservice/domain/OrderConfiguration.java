package net.chrisrichardson.ftgo.orderservice.domain;

import io.micrometer.core.instrument.MeterRegistry;
import net.chrisrichardson.ftgo.domain.*;
import net.chrisrichardson.ftgo.orderservice.client.ConsumerServiceClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

@Configuration
@Import(DomainConfiguration.class)
public class OrderConfiguration {
  // TODO move to framework
  @Bean
  public CourierAssignmentStrategy courierAssignmentStrategy() {
    return new DistanceOptimizedCourierAssignmentStrategy();
  }

  @Bean
  public RestTemplate restTemplate() {
    return new RestTemplate();
  }

  @Bean
  public ConsumerServiceClient consumerServiceClient(RestTemplate restTemplate,
                                                     @Value("${consumer.service.url:http://localhost:8082}") String consumerServiceUrl) {
    return new ConsumerServiceClient(restTemplate, consumerServiceUrl);
  }

  @Bean
  public OrderService orderService(RestaurantRepository restaurantRepository,
                                   OrderRepository orderRepository,
                                   Optional<MeterRegistry> meterRegistry,
                                   ConsumerServiceClient consumerServiceClient,
                                   CourierRepository courierRepository,
                                   CourierAssignmentStrategy courierAssignmentStrategy,
                                   PlatformTransactionManager transactionManager) {
    return new OrderService(orderRepository,
            restaurantRepository,
            meterRegistry,
            consumerServiceClient,
            courierRepository,
            courierAssignmentStrategy,
            new TransactionTemplate(transactionManager));
  }

  @Bean
  public MeterRegistryCustomizer meterRegistryCustomizer(@Value("${spring.application.name}") String serviceName) {
    return registry -> registry.config().commonTags("service", serviceName);
  }
}
