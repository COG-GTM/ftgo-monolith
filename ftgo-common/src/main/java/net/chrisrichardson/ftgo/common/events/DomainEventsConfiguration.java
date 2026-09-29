package net.chrisrichardson.ftgo.common.events;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DomainEventsConfiguration {

  @Bean
  public DomainEventPublisher domainEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
    return new InProcessDomainEventPublisher(applicationEventPublisher);
  }
}
