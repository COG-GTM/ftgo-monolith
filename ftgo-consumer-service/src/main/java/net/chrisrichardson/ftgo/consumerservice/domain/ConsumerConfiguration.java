package net.chrisrichardson.ftgo.consumerservice.domain;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ConsumerConfiguration {

  @Bean
  public ConsumerService consumerService() {
    return new ConsumerService();
  }
}
