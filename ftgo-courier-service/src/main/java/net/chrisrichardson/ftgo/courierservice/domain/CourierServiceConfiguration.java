package net.chrisrichardson.ftgo.courierservice.domain;

import net.chrisrichardson.ftgo.common.CommonConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({CommonConfiguration.class, CourierPersistenceConfiguration.class})
public class CourierServiceConfiguration {

  @Bean
  public CourierAssignmentStrategy courierAssignmentStrategy() {
    return new DistanceOptimizedCourierAssignmentStrategy();
  }

  @Bean
  public CourierService courierService(CourierRepository courierRepository, CourierAssignmentStrategy courierAssignmentStrategy) {
    return new CourierService(courierRepository, courierAssignmentStrategy);
  }
}
