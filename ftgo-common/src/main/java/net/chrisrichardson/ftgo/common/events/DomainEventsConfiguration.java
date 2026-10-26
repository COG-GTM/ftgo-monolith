package net.chrisrichardson.ftgo.common.events;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Configuration
public class DomainEventsConfiguration {

  @Bean
  public DomainEventPublisher domainEventPublisher(Optional<List<DomainEventSubscriber<?>>> subscribers) {
    return new TransactionAwareDomainEventPublisher(
            new InProcessDomainEventBus(subscribers.orElse(Collections.emptyList())));
  }
}
