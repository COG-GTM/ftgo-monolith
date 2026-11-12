package net.chrisrichardson.ftgo;

import net.chrisrichardson.ftgo.consumerservice.main.ConsumerServiceConfiguration;
import net.chrisrichardson.ftgo.endtoendtests.common.AbstractEndToEndTests;
import net.chrisrichardson.ftgo.orderservice.main.OrderServiceConfiguration;
import net.chrisrichardson.ftgo.restaurantservice.RestaurantServiceConfiguration;
import org.junit.runner.RunWith;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes=FtgoApplicationTest.Config.class,
        properties = {
                "ftgo.security.operator.username=ftgo-application-test-operator",
                "ftgo.security.operator.password=" + FtgoApplicationTest.OPERATOR_PASSWORD
        },
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class FtgoApplicationTest extends AbstractEndToEndTests {

  static final String OPERATOR_PASSWORD = "e2e-operator-password";

  @Configuration
  @EnableAutoConfiguration
  @ComponentScan
  @Import({ConsumerServiceConfiguration.class,
          OrderServiceConfiguration.class,
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

  @Override
  public String getOperatorUsername() {
    return "ftgo-application-test-operator";
  }

  @Override
  public String getOperatorPassword() {
    return OPERATOR_PASSWORD;
  }
}
