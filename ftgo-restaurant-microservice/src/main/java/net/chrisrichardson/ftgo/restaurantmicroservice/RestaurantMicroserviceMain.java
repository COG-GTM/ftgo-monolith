package net.chrisrichardson.ftgo.restaurantmicroservice;

import net.chrisrichardson.eventstore.examples.customersandorders.commonswagger.CommonSwaggerConfiguration;
import net.chrisrichardson.ftgo.restaurantmicroservice.domain.RestaurantDomainConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableAutoConfiguration
@ComponentScan
@EntityScan(basePackages = {"net.chrisrichardson.ftgo.restaurantmicroservice.domain"})
@EnableJpaRepositories(basePackages = {"net.chrisrichardson.ftgo.restaurantmicroservice.domain"})
@Import({RestaurantDomainConfiguration.class, CommonSwaggerConfiguration.class})
public class RestaurantMicroserviceMain {

  public static void main(String[] args) {
    SpringApplication.run(RestaurantMicroserviceMain.class, args);
  }
}
