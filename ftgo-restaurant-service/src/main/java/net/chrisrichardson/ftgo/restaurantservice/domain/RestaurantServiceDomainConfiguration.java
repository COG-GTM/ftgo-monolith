package net.chrisrichardson.ftgo.restaurantservice.domain;

import net.chrisrichardson.ftgo.restaurantservice.api.client.RestaurantServiceClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EntityScan(basePackageClasses = Restaurant.class)
@EnableJpaRepositories(basePackageClasses = RestaurantRepository.class)
public class RestaurantServiceDomainConfiguration {

  @Bean
  public RestaurantService restaurantService(RestaurantRepository restaurantRepository) {
    return new RestaurantService(restaurantRepository);
  }

  @Bean
  @ConditionalOnProperty(name = RestaurantServiceClient.MODE_PROPERTY,
          havingValue = RestaurantServiceClient.MODE_IN_PROCESS, matchIfMissing = true)
  public RestaurantServiceClient inProcessRestaurantServiceClient(RestaurantService restaurantService) {
    return new InProcessRestaurantServiceClient(restaurantService);
  }
}
