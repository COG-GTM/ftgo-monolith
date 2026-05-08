package net.chrisrichardson.ftgo.orderservice.domain;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.MeterRegistry;
import net.chrisrichardson.ftgo.domain.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
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
  public RestTemplate restTemplate(ObjectMapper objectMapper) {
    RestTemplate restTemplate = new RestTemplate();
    restTemplate.getMessageConverters().stream()
            .filter(MappingJackson2HttpMessageConverter.class::isInstance)
            .map(MappingJackson2HttpMessageConverter.class::cast)
            .forEach(c -> c.setObjectMapper(objectMapper));
    return restTemplate;
  }

  @Bean
  public MeterRegistryCustomizer meterRegistryCustomizer(@Value("${spring.application.name}") String serviceName) {
    return registry -> registry.config().commonTags("service", serviceName);
  }
}
