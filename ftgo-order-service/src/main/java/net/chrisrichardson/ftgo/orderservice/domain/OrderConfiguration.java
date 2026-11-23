package net.chrisrichardson.ftgo.orderservice.domain;

import io.micrometer.core.instrument.MeterRegistry;
import net.chrisrichardson.ftgo.common.CommonConfiguration;
import net.chrisrichardson.ftgo.consumerservice.api.ConsumerValidationApi;
import net.chrisrichardson.ftgo.courierservice.api.CourierAvailabilityApi;
import net.chrisrichardson.ftgo.restaurantservice.api.RestaurantLookupApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.util.Optional;

@Configuration
@Import({CommonConfiguration.class, OrderPersistenceConfiguration.class})
public class OrderConfiguration {

  @Bean
  public OrderService orderService(OrderRepository orderRepository,
                                   RestaurantLookupApi restaurantLookupApi,
                                   Optional<MeterRegistry> meterRegistry,
                                   ConsumerValidationApi consumerValidationApi,
                                   CourierAvailabilityApi courierAvailabilityApi) {
    return new OrderService(orderRepository,
            restaurantLookupApi,
            meterRegistry,
            consumerValidationApi,
            courierAvailabilityApi);
  }

  @Bean
  public MeterRegistryCustomizer meterRegistryCustomizer(@Value("${spring.application.name}") String serviceName) {
    return registry -> registry.config().commonTags("service", serviceName);
  }
}
