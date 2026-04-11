package net.chrisrichardson.ftgo.courierservice.main;

import net.chrisrichardson.ftgo.courierservice.web.CourierWebConfiguration;
import net.chrisrichardson.eventstore.examples.customersandorders.commonswagger.CommonSwaggerConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import({CourierWebConfiguration.class, CommonSwaggerConfiguration.class})
public class CourierServiceMain {

  public static void main(String[] args) {
    SpringApplication.run(CourierServiceMain.class, args);
  }
}
