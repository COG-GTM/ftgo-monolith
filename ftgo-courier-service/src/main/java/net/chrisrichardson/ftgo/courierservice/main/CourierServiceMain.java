package net.chrisrichardson.ftgo.courierservice.main;

import net.chrisrichardson.ftgo.courierservice.domain.CourierServiceConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import(CourierServiceConfiguration.class)
public class CourierServiceMain {
  public static void main(String[] args) {
    SpringApplication.run(CourierServiceMain.class, args);
  }
}
