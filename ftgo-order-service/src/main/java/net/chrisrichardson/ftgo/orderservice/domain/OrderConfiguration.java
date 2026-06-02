package net.chrisrichardson.ftgo.orderservice.domain;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.MeterRegistry;
import net.chrisrichardson.ftgo.consumerservice.api.ConsumerServiceProxy;
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
  public RestTemplate consumerServiceRestTemplate(ObjectMapper objectMapper) {
    RestTemplate restTemplate = new RestTemplate();
    MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
    converter.setObjectMapper(objectMapper);
    restTemplate.getMessageConverters().add(0, converter);
    return restTemplate;
  }

  @Bean
  public ConsumerServiceProxy consumerServiceProxy(@Value("${consumer.service.url:http://localhost:8082}") String consumerServiceUrl,
                                                   RestTemplate consumerServiceRestTemplate) {
    return new ConsumerServiceHttpProxy(consumerServiceUrl, consumerServiceRestTemplate);
  }

  @Bean
  public OrderService orderService(RestaurantRepository restaurantRepository,
                                   OrderRepository orderRepository,
                                   Optional<MeterRegistry> meterRegistry,
                                   ConsumerServiceProxy consumerServiceProxy,
                                   CourierRepository courierRepository,
                                   CourierAssignmentStrategy courierAssignmentStrategy) {
    return new OrderService(orderRepository,
            restaurantRepository,
            meterRegistry,
            consumerServiceProxy,
            courierRepository,
            courierAssignmentStrategy);
  }

  @Bean
  public MeterRegistryCustomizer meterRegistryCustomizer(@Value("${spring.application.name}") String serviceName) {
    return registry -> registry.config().commonTags("service", serviceName);
  }
}
