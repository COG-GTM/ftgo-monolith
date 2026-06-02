package net.chrisrichardson.ftgo;

import net.chrisrichardson.ftgo.endtoendtests.common.AbstractEndToEndTests;
import net.chrisrichardson.ftgo.orderservice.main.OrderServiceConfiguration;
import net.chrisrichardson.ftgo.restaurantservice.RestaurantServiceConfiguration;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;

// Consumer management has been extracted into the standalone ftgo-consumer-service
// (default port 8082), so the monolith application context no longer exposes the
// POST /consumers endpoint. AbstractEndToEndTests#createConsumer (private) posts to
// /consumers on the application port, which now returns 404, so this in-process
// end-to-end test can no longer run against the monolith alone. End-to-end consumer
// + order flows must be exercised against the full multi-service stack started via
// docker-compose (see ftgo-end-to-end-tests).
@Ignore("Requires the standalone ftgo-consumer-service; run end-to-end via docker-compose")
@RunWith(SpringRunner.class)
@SpringBootTest(classes=FtgoApplicationTest.Config.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class FtgoApplicationTest extends AbstractEndToEndTests {

  @Configuration
  @EnableAutoConfiguration
  @ComponentScan
  @Import({OrderServiceConfiguration.class,
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
