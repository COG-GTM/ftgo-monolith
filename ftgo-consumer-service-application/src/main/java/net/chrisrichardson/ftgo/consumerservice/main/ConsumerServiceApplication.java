package net.chrisrichardson.ftgo.consumerservice.main;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableAutoConfiguration
@ComponentScan(basePackages = {
        "net.chrisrichardson.ftgo.consumerservice.domain",
        "net.chrisrichardson.ftgo.consumerservice.web"
})
@EntityScan(basePackages = "net.chrisrichardson.ftgo.consumerservice.domain")
@EnableJpaRepositories(basePackages = "net.chrisrichardson.ftgo.consumerservice.domain")
public class ConsumerServiceApplication {

  public static void main(String[] args) {
    SpringApplication.run(ConsumerServiceApplication.class, args);
  }
}
