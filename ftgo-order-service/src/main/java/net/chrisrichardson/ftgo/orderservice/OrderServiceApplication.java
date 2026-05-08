package net.chrisrichardson.ftgo.orderservice;

import net.chrisrichardson.ftgo.common.tracking.ApiTrackingConfiguration;
import net.chrisrichardson.ftgo.orderservice.domain.OrderServiceWithRepositoriesConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import({OrderServiceWithRepositoriesConfiguration.class, ApiTrackingConfiguration.class})
public class OrderServiceApplication {

  public static void main(String[] args) {
    SpringApplication.run(OrderServiceApplication.class, args);
  }
}
