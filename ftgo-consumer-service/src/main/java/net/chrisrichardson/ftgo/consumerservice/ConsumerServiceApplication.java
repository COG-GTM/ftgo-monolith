package net.chrisrichardson.ftgo.consumerservice;

import net.chrisrichardson.ftgo.consumerservice.web.ConsumerWebConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import(ConsumerWebConfiguration.class)
public class ConsumerServiceApplication {

  public static void main(String[] args) {
    SpringApplication.run(ConsumerServiceApplication.class, args);
  }
}
