package net.chrisrichardson.ftgo.restaurantservice.domain;

import net.chrisrichardson.ftgo.domain.DomainConfiguration;
import net.chrisrichardson.ftgo.domain.RestaurantRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(DomainConfiguration.class)
public class RestaurantServiceDomainConfiguration {

  @Bean
  public RestaurantService restaurantService(RestaurantRepository restaurantRepository) {
    return new RestaurantService(restaurantRepository);
  }
}
