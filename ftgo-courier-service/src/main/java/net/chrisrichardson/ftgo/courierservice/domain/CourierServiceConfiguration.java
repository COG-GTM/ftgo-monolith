package net.chrisrichardson.ftgo.courierservice.domain;

import net.chrisrichardson.ftgo.domain.CourierRepository;
import net.chrisrichardson.ftgo.domain.DomainConfiguration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.time.Duration;

@Configuration
@EnableScheduling
@Import(DomainConfiguration.class)
public class CourierServiceConfiguration {

  @Bean
  public CourierService courierService(CourierRepository courierRepository) {
    return new CourierService(courierRepository);
  }

  @Bean
  public CourierLocationRetentionJob courierLocationRetentionJob(CourierService courierService,
                                                                 @Value("${ftgo.courier.location.retention}") Duration retention) {
    return new CourierLocationRetentionJob(courierService, retention);
  }

}
