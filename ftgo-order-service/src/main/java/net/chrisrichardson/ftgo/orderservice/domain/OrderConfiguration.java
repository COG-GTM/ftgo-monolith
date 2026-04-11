package net.chrisrichardson.ftgo.orderservice.domain;

import io.micrometer.core.instrument.MeterRegistry;
import net.chrisrichardson.ftgo.domain.DomainConfiguration;
import net.chrisrichardson.ftgo.domain.OrderRepository;
import net.chrisrichardson.ftgo.orderservice.clients.ConsumerClient;
import net.chrisrichardson.ftgo.orderservice.clients.CourierClient;
import net.chrisrichardson.ftgo.orderservice.clients.RestaurantClient;
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
  // TODO move to framework
  @Bean
  public RestTemplate restTemplate() {
    return new RestTemplate();
  }

  @Bean
  public OrderService orderService(OrderRepository orderRepository,
                                   RestaurantClient restaurantClient,
                                   Optional<MeterRegistry> meterRegistry,
                                   ConsumerClient consumerClient,
                                   CourierClient courierClient) {
    return new OrderService(orderRepository,
            restaurantClient,
            meterRegistry,
            consumerClient,
            courierClient);
  }

  @Bean
  public MeterRegistryCustomizer meterRegistryCustomizer(@Value("${spring.application.name}") String serviceName) {
    return registry -> registry.config().commonTags("service", serviceName);
  }
}
