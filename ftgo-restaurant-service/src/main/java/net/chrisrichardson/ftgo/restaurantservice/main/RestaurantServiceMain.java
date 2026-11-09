package net.chrisrichardson.ftgo.restaurantservice.main;

import net.chrisrichardson.ftgo.restaurantservice.RestaurantServiceConfiguration;
import org.springframework.boot.SpringApplication;

import java.util.Collections;
import java.util.Map;

/**
 * Entry point for running the restaurant service as its own deployable. Deliberately not a Spring component,
 * so the monolith's component scan never picks up standalone-only settings.
 */
public class RestaurantServiceMain {

  public static final String CONFIG_NAME = "restaurant-service";

  public static Map<String, Object> defaultProperties() {
    return Collections.singletonMap("spring.config.name", CONFIG_NAME);
  }

  public static void main(String[] args) {
    SpringApplication application = new SpringApplication(RestaurantServiceConfiguration.class);
    application.setDefaultProperties(defaultProperties());
    application.run(args);
  }
}
