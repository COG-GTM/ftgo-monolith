package net.chrisrichardson.ftgo.restaurant;

import net.chrisrichardson.eventstore.examples.customersandorders.commonswagger.CommonSwaggerConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import({CommonSwaggerConfiguration.class})
public class RestaurantServiceApplication {

  public static void main(String[] args) {
    SpringApplication.run(RestaurantServiceApplication.class, args);
  }
}
