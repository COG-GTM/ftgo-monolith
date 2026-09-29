package net.chrisrichardson.ftgo.common.events;

/**
 * Receives events of type {@code E} (including subtypes).
 * Spring beans implementing this interface are registered with the in-process event bus automatically.
 */
public interface DomainEventSubscriber<E extends DomainEvent> {

  Class<E> getEventType();

  void handle(E event);
}
