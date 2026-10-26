package net.chrisrichardson.ftgo.consumerservice.main;

import net.chrisrichardson.ftgo.consumerservice.web.ConsumerWebConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Standalone Spring Boot entry point for the extracted consumer microservice.
 *
 * <p>Runs independently of the FTGO monolith on its own port with its own
 * (H2 in-memory) database. Persistence for the consumer bounded context lives
 * in {@code net.chrisrichardson.ftgo.consumerservice.domain}.
 */
@Configuration
@EnableAutoConfiguration
@ComponentScan
@EntityScan(basePackages = "net.chrisrichardson.ftgo.consumerservice.domain")
@EnableJpaRepositories(basePackages = "net.chrisrichardson.ftgo.consumerservice.domain")
@Import(ConsumerWebConfiguration.class)
public class ConsumerServiceApplicationMain {

  public static void main(String[] args) {
    SpringApplication.run(ConsumerServiceApplicationMain.class, args);
  }
}
