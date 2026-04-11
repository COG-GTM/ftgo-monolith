package net.chrisrichardson.ftgo.courierservice.domain;

import net.chrisrichardson.ftgo.common.CommonConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@Import(CommonConfiguration.class)
@EntityScan(basePackages = "net.chrisrichardson.ftgo.courierservice.domain")
@EnableJpaRepositories(basePackages = "net.chrisrichardson.ftgo.courierservice.domain")
public class CourierServiceConfiguration {

  @Bean
  public CourierService courierService(CourierRepository courierRepository) {
    return new CourierService(courierRepository);
  }

}
