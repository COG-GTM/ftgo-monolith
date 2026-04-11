package net.chrisrichardson.ftgo.orderservice.main;

import net.chrisrichardson.ftgo.orderservice.domain.OrderServiceWithRepositoriesConfiguration;
import net.chrisrichardson.eventstore.examples.customersandorders.commonswagger.CommonSwaggerConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import({OrderServiceWithRepositoriesConfiguration.class, CommonSwaggerConfiguration.class})
public class OrderServiceApplication {

  public static void main(String[] args) {
    SpringApplication.run(OrderServiceApplication.class, args);
  }
}
