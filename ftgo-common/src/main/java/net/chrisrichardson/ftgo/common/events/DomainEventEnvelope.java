package net.chrisrichardson.ftgo.common.events;

import org.springframework.core.ResolvableType;
import org.springframework.core.ResolvableTypeProvider;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Wraps a {@link DomainEvent} with the metadata a message broker would need: the id and type of the
 * aggregate that emitted it, a unique event id and the time it was published.
 *
 * <p>Implements {@link ResolvableTypeProvider} so Spring listeners can subscribe to a specific event type,
 * e.g. {@code @EventListener void on(DomainEventEnvelope<OrderCancelledEvent> envelope)}, or to a family of
 * events, e.g. {@code DomainEventEnvelope<? extends OrderStateChangedEvent>}.
 */
public class DomainEventEnvelope<E extends DomainEvent> implements ResolvableTypeProvider {

  private final String eventId;
  private final String aggregateType;
  private final String aggregateId;
  private final Instant publishedAt;
  private final E event;

  public DomainEventEnvelope(String eventId, String aggregateType, String aggregateId, Instant publishedAt, E event) {
    this.eventId = Objects.requireNonNull(eventId, "eventId");
    this.aggregateType = Objects.requireNonNull(aggregateType, "aggregateType");
    this.aggregateId = Objects.requireNonNull(aggregateId, "aggregateId");
    this.publishedAt = Objects.requireNonNull(publishedAt, "publishedAt");
    this.event = Objects.requireNonNull(event, "event");
  }

  public static <E extends DomainEvent> DomainEventEnvelope<E> wrap(String aggregateType, Object aggregateId, E event) {
    return new DomainEventEnvelope<>(UUID.randomUUID().toString(), aggregateType,
            String.valueOf(Objects.requireNonNull(aggregateId, "aggregateId")), Instant.now(), event);
  }

  public String getEventId() {
    return eventId;
  }

  public String getAggregateType() {
    return aggregateType;
  }

  public String getAggregateId() {
    return aggregateId;
  }

  public Instant getPublishedAt() {
    return publishedAt;
  }

  public E getEvent() {
    return event;
  }

  public String getEventType() {
    return event.getClass().getName();
  }

  @Override
  public ResolvableType getResolvableType() {
    return ResolvableType.forClassWithGenerics(DomainEventEnvelope.class, event.getClass());
  }

  @Override
  public String toString() {
    return "DomainEventEnvelope{eventId=" + eventId +
            ", aggregateType=" + aggregateType +
            ", aggregateId=" + aggregateId +
            ", publishedAt=" + publishedAt +
            ", event=" + event + "}";
  }
}
