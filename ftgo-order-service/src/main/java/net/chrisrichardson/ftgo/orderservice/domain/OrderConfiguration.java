package net.chrisrichardson.ftgo.orderservice.domain;

import io.micrometer.core.instrument.MeterRegistry;
import net.chrisrichardson.ftgo.common.CommonConfiguration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

@Configuration
@Import(CommonConfiguration.class)
public class OrderConfiguration {

  @Bean
  public RestTemplate restTemplate() {
    return new RestTemplate();
  }

  @Bean
  public ConsumerValidation consumerServiceClient(RestTemplate restTemplate,
                                                  @Value("${services.consumer-service.url:http://consumer-service:8080}") String consumerServiceUrl) {
    return new ConsumerServiceClient(restTemplate, consumerServiceUrl);
  }

  @Bean
  public RestaurantServiceClient restaurantServiceClient(RestTemplate restTemplate,
                                                         @Value("${services.restaurant-service.url:http://restaurant-service:8080}") String restaurantServiceUrl) {
    return new RestaurantServiceClient(restTemplate, restaurantServiceUrl);
  }

  @Bean
  public CourierServiceClient courierServiceClient(RestTemplate restTemplate,
                                                   @Value("${services.courier-service.url:http://courier-service:8080}") String courierServiceUrl) {
    return new CourierServiceClient(restTemplate, courierServiceUrl);
  }

  @Bean
  public OrderService orderService(OrderRepository orderRepository,
                                   RestaurantServiceClient restaurantServiceClient,
                                   Optional<MeterRegistry> meterRegistry,
                                   ConsumerValidation consumerValidation,
                                   CourierServiceClient courierServiceClient) {
    return new OrderService(orderRepository,
            restaurantServiceClient,
            meterRegistry,
            consumerValidation,
            courierServiceClient);
  }

  @Bean
  public MeterRegistryCustomizer meterRegistryCustomizer(@Value("${spring.application.name}") String serviceName) {
    return registry -> registry.config().commonTags("service", serviceName);
  }
}
