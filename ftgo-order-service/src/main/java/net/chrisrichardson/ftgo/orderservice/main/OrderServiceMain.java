package net.chrisrichardson.ftgo.orderservice.main;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import(OrderServiceConfiguration.class)
public class OrderServiceMain {

  public static void main(String[] args) {
    SpringApplication.run(OrderServiceMain.class, args);
  }
}
