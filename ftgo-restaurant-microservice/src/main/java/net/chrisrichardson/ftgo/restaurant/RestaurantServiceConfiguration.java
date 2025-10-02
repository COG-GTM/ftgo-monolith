package net.chrisrichardson.ftgo.restaurant;

import net.chrisrichardson.ftgo.restaurant.domain.RestaurantService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories
public class RestaurantServiceConfiguration {

  @Bean
  public RestaurantService restaurantService() {
    return new RestaurantService();
  }
}
