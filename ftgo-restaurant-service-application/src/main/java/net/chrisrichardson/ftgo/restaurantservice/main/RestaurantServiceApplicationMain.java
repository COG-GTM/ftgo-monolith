package net.chrisrichardson.ftgo.restaurantservice.main;

import net.chrisrichardson.ftgo.restaurantservice.RestaurantServiceConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@EnableAutoConfiguration
@ComponentScan
@Import(RestaurantServiceConfiguration.class)
public class RestaurantServiceApplicationMain {

  public static void main(String[] args) {
    SpringApplication.run(RestaurantServiceApplicationMain.class, args);
  }
}
