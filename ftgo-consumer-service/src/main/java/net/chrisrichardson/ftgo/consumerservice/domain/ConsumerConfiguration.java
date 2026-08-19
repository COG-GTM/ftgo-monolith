package net.chrisrichardson.ftgo.consumerservice.domain;

import net.chrisrichardson.ftgo.common.security.StaffAuthenticator;
import net.chrisrichardson.ftgo.domain.ConsumerRepository;
import net.chrisrichardson.ftgo.domain.DomainConfiguration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(DomainConfiguration.class)
public class ConsumerConfiguration {

  @Bean
  public ConsumerService consumerService() {
    return new ConsumerService();
  }

  @Bean
  public ConsumerAuthenticator consumerAuthenticator(ConsumerRepository consumerRepository) {
    return new ConsumerAuthenticator(consumerRepository);
  }

  @Bean
  public StaffAuthenticator staffAuthenticator(@Value("${ftgo.staff.api-token:}") String staffApiToken) {
    return new StaffAuthenticator(staffApiToken);
  }
}
