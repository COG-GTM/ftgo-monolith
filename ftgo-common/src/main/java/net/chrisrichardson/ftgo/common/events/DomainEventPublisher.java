package net.chrisrichardson.ftgo.common.events;

import java.util.List;

/**
 * Publishes the events emitted by an aggregate.
 *
 * <p>The monolith uses an in-process implementation; a message-broker backed implementation (e.g. a
 * transactional outbox) can replace it without changing the code that emits events.
 */
public interface DomainEventPublisher {

  void publish(String aggregateType, Object aggregateId, List<? extends DomainEvent> domainEvents);

  default void publish(Class<?> aggregateType, Object aggregateId, List<? extends DomainEvent> domainEvents) {
    publish(aggregateType.getName(), aggregateId, domainEvents);
  }
}
