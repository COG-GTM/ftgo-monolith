package net.chrisrichardson.ftgo.common;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableAutoConfiguration
@ComponentScan
@EntityScan(basePackages = {"net.chrisrichardson.ftgo.common.tracking"})
@EnableJpaRepositories(basePackages = {"net.chrisrichardson.ftgo.common.tracking"})
public class CommonConfiguration {

  @Bean
  public MoneyModule moneyModule() {
    return new MoneyModule();
  }
}
