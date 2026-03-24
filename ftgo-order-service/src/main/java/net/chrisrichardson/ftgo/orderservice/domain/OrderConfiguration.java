package net.chrisrichardson.ftgo.orderservice.domain;

import io.micrometer.core.instrument.MeterRegistry;
import net.chrisrichardson.ftgo.common.CommonConfiguration;
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
@Import(CommonConfiguration.class)
public class OrderConfiguration {

  @Bean
  public RestTemplate restTemplate() {
    return new RestTemplate();
  }

  @Bean
  public ConsumerServiceClient consumerServiceClient(RestTemplate restTemplate,
                                                      @Value("${services.consumer.url}") String consumerServiceUrl) {
    return new ConsumerServiceClient(restTemplate, consumerServiceUrl);
  }

  @Bean
  public RestaurantServiceClient restaurantServiceClient(RestTemplate restTemplate,
                                                          @Value("${services.restaurant.url}") String restaurantServiceUrl) {
    return new RestaurantServiceClient(restTemplate, restaurantServiceUrl);
  }

  @Bean
  public CourierServiceClient courierServiceClient(RestTemplate restTemplate,
                                                    @Value("${services.courier.url}") String courierServiceUrl) {
    return new CourierServiceClient(restTemplate, courierServiceUrl);
  }

  @Bean
  public OrderService orderService(OrderRepository orderRepository,
                                   RestaurantServiceClient restaurantServiceClient,
                                   Optional<MeterRegistry> meterRegistry,
                                   ConsumerServiceClient consumerServiceClient,
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
