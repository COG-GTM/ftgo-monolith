package net.chrisrichardson.ftgo;

import net.chrisrichardson.ftgo.common.tracking.ApiTrackingConfiguration;
import net.chrisrichardson.ftgo.consumerservice.main.ConsumerServiceConfiguration;
import net.chrisrichardson.ftgo.courierservice.web.CourierWebConfiguration;
import net.chrisrichardson.ftgo.orderservice.main.OrderServiceConfiguration;
import net.chrisrichardson.ftgo.restaurantservice.RestaurantServiceConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@EnableAutoConfiguration
@ComponentScan
@Import({ConsumerServiceConfiguration.class,
        OrderServiceConfiguration.class,
        RestaurantServiceConfiguration.class,
        CourierWebConfiguration.class,
        ApiTrackingConfiguration.class})
public class FtgoApplicationMain {

  public static void main(String[] args) {
    if (isEmpty(System.getenv("SPRING_DATASOURCE_PASSWORD"))
        && isEmpty(System.getenv("MYSQL_PASSWORD"))) {
      throw new IllegalStateException(
          "MYSQL_PASSWORD or SPRING_DATASOURCE_PASSWORD must be set; run ./create-local-env.sh or export them");
    }
    SpringApplication.run(FtgoApplicationMain.class, args);
  }

  private static boolean isEmpty(String value) {
    return value == null || value.trim().isEmpty();
  }
}
