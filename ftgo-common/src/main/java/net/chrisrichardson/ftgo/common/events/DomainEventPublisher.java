package net.chrisrichardson.ftgo.common.events;

import java.util.List;

/**
 * Port through which the domain publishes events.
 * Callers depend only on this interface so the in-process implementation can be
 * replaced by a message-broker-backed one (e.g. Kafka, or a transactional outbox)
 * without changing publishers.
 */
public interface DomainEventPublisher {

  void publish(DomainEvent event);

  default void publish(List<? extends DomainEvent> events) {
    events.forEach(this::publish);
  }
}
