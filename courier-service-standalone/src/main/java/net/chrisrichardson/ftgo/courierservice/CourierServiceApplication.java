package net.chrisrichardson.ftgo.courierservice;

import net.chrisrichardson.ftgo.courierservice.web.CourierWebConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import(CourierWebConfiguration.class)
public class CourierServiceApplication {

  public static void main(String[] args) {
    SpringApplication.run(CourierServiceApplication.class, args);
  }
}
