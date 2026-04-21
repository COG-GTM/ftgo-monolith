package net.chrisrichardson.ftgo.courierservice.main;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@ComponentScan(basePackages = {
        "net.chrisrichardson.ftgo.courierservice",
        "net.chrisrichardson.ftgo.domain",
        "net.chrisrichardson.ftgo.common"
})
@EntityScan(basePackages = {
        "net.chrisrichardson.ftgo.domain",
        "net.chrisrichardson.ftgo.common"
})
@EnableJpaRepositories(basePackages = "net.chrisrichardson.ftgo.domain")
public class CourierServiceApplication {

  public static void main(String[] args) {
    SpringApplication.run(CourierServiceApplication.class, args);
  }
}
