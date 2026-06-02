package net.chrisrichardson.ftgo.courierservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableAutoConfiguration
@ComponentScan
@EntityScan(basePackages = "net.chrisrichardson.ftgo.courierservice.domain")
@EnableJpaRepositories(basePackages = "net.chrisrichardson.ftgo.courierservice.domain")
public class CourierServiceApplication {

  public static void main(String[] args) {
    SpringApplication.run(CourierServiceApplication.class, args);
  }
}
