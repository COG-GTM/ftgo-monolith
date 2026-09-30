package net.chrisrichardson.ftgo.courierservice.web;

import net.chrisrichardson.ftgo.courierservice.domain.CourierService;
import net.chrisrichardson.ftgo.courierservice.domain.CourierServiceConfiguration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(CourierServiceConfiguration.class)
@ComponentScan
public class CourierWebConfiguration {

  @Bean
  public CourierAuthorizer courierAuthorizer(CourierService courierService,
                                             @Value("${ftgo.courier.dispatcher-api-key:}") String dispatcherApiKey) {
    return new CourierAuthorizer(courierService, dispatcherApiKey);
  }
}
