package net.chrisrichardson.ftgo;

import net.chrisrichardson.ftgo.consumerservice.main.ConsumerServiceConfiguration;
import net.chrisrichardson.ftgo.endtoendtests.common.AbstractEndToEndTests;
import net.chrisrichardson.ftgo.orderservice.client.OrderServiceClientConfiguration;
import net.chrisrichardson.ftgo.orderservice.main.OrderServiceMain;
import net.chrisrichardson.ftgo.restaurantservice.RestaurantServiceConfiguration;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes=FtgoApplicationTest.Config.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class FtgoApplicationTest extends AbstractEndToEndTests {

  private static ConfigurableApplicationContext orderServiceContext;
  private static String previousOrderServiceUrl;

  @BeforeClass
  public static void startOrderService() {
    previousOrderServiceUrl = System.getProperty("order.service.url");
    orderServiceContext = new SpringApplicationBuilder(OrderServiceMain.class)
            .properties("server.port=0")
            .run("--server.port=0");
    Integer orderServicePort = orderServiceContext.getEnvironment()
            .getProperty("local.server.port", Integer.class);
    System.setProperty("order.service.url", "http://localhost:" + orderServicePort);
  }

  @AfterClass
  public static void stopOrderService() {
    if (orderServiceContext != null) {
      orderServiceContext.close();
    }
    if (previousOrderServiceUrl == null) {
      System.clearProperty("order.service.url");
    } else {
      System.setProperty("order.service.url", previousOrderServiceUrl);
    }
  }

  @Configuration
  @EnableAutoConfiguration
  @ComponentScan(excludeFilters = @ComponentScan.Filter(
          type = FilterType.REGEX, pattern = "net\\.chrisrichardson\\.ftgo\\.orderservice\\..*"))
  @Import({ConsumerServiceConfiguration.class,
          OrderServiceClientConfiguration.class,
          RestaurantServiceConfiguration.class})
  public static class Config {

  }

  @LocalServerPort
  private int port;

  @Override
  public String getHost() {
    return "localhost";
  }

  @Override
  public int getApplicationPort() {
    return port;
  }
}
