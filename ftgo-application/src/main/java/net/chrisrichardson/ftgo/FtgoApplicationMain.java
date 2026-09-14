package net.chrisrichardson.ftgo;

import net.chrisrichardson.ftgo.common.tracking.ApiTrackingConfiguration;
import net.chrisrichardson.ftgo.consumerservice.main.ConsumerServiceConfiguration;
import net.chrisrichardson.ftgo.courierservice.web.CourierWebConfiguration;
import net.chrisrichardson.ftgo.orderservice.client.OrderServiceClientConfiguration;
import net.chrisrichardson.ftgo.restaurantservice.RestaurantServiceConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;

@Configuration
@EnableAutoConfiguration
@ComponentScan(excludeFilters = @ComponentScan.Filter(
        type = FilterType.REGEX, pattern = "net\\.chrisrichardson\\.ftgo\\.orderservice\\..*"))
@Import({ConsumerServiceConfiguration.class,
        OrderServiceClientConfiguration.class,
        RestaurantServiceConfiguration.class,
        CourierWebConfiguration.class,
        ApiTrackingConfiguration.class})
public class FtgoApplicationMain {

  public static void main(String[] args) {
    SpringApplication.run(FtgoApplicationMain.class, args);
  }
}
