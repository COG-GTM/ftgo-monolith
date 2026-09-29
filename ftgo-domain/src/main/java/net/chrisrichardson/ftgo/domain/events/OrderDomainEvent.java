package net.chrisrichardson.ftgo.domain.events;

import net.chrisrichardson.ftgo.common.events.DomainEvent;

import java.time.LocalDateTime;

/**
 * Base class for events emitted by the {@code Order} aggregate. The order id travels in the
 * {@link net.chrisrichardson.ftgo.common.events.DomainEventEnvelope}, since it is not assigned until the
 * order is first persisted.
 */
public abstract class OrderDomainEvent implements DomainEvent {

  private final LocalDateTime occurredAt;

  protected OrderDomainEvent() {
    this.occurredAt = LocalDateTime.now();
  }

  public LocalDateTime getOccurredAt() {
    return occurredAt;
  }
}
