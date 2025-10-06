package net.chrisrichardson.ftgo.restaurantservice.main;

import net.chrisrichardson.ftgo.restaurantservice.RestaurantServiceConfiguration;
import net.chrisrichardson.eventstore.examples.customersandorders.commonswagger.CommonSwaggerConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import({RestaurantServiceConfiguration.class, CommonSwaggerConfiguration.class})
public class RestaurantServiceMain {

  public static void main(String[] args) {
    SpringApplication.run(RestaurantServiceMain.class, args);
  }
}
