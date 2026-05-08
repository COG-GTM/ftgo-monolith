package net.chrisrichardson.ftgo.consumerservice.domain;

import net.chrisrichardson.ftgo.common.CommonConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableAutoConfiguration
@EnableJpaRepositories
@Import(CommonConfiguration.class)
public class ConsumerConfiguration {

  @Bean
  public ConsumerService consumerService(ConsumerRepository consumerRepository) {
    return new ConsumerService(consumerRepository);
  }
}
