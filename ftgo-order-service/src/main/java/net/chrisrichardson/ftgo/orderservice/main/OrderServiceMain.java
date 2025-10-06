package net.chrisrichardson.ftgo.orderservice.main;

import net.chrisrichardson.ftgo.orderservice.web.OrderWebConfiguration;
import net.chrisrichardson.eventstore.examples.customersandorders.commonswagger.CommonSwaggerConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import({OrderWebConfiguration.class, CommonSwaggerConfiguration.class})
public class OrderServiceMain {

  public static void main(String[] args) {
    SpringApplication.run(OrderServiceMain.class, args);
  }
}
