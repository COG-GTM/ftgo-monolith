package net.chrisrichardson.ftgo.restaurantservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import(RestaurantServiceConfiguration.class)
public class RestaurantServiceMain {

  public static void main(String[] args) {
    SpringApplication.run(RestaurantServiceMain.class, args);
  }
}
