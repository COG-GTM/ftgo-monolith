package net.chrisrichardson.ftgo.consumerservice;

import net.chrisrichardson.ftgo.consumerservice.main.ConsumerServiceConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import(ConsumerServiceConfiguration.class)
public class ConsumerServiceApplication {

  public static void main(String[] args) {
    SpringApplication.run(ConsumerServiceApplication.class, args);
  }
}
