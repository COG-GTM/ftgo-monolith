package net.chrisrichardson.ftgo.common.events;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * Synchronous, in-memory {@link DomainEventPublisher} that dispatches each event to every subscriber
 * whose event type is assignable from the event's class, in registration order.
 * A failing subscriber is logged and does not prevent delivery to the remaining subscribers,
 * nor does it propagate to the publisher.
 */
public class InProcessDomainEventBus implements DomainEventPublisher {

  private static final Logger logger = LoggerFactory.getLogger(InProcessDomainEventBus.class);

  private final List<DomainEventSubscriber<?>> subscribers = new CopyOnWriteArrayList<>();

  public InProcessDomainEventBus() {
    this(Collections.emptyList());
  }

  public InProcessDomainEventBus(List<? extends DomainEventSubscriber<?>> subscribers) {
    subscribers.forEach(this::subscribe);
  }

  public void subscribe(DomainEventSubscriber<?> subscriber) {
    subscribers.add(subscriber);
  }

  public <E extends DomainEvent> DomainEventSubscriber<E> subscribe(Class<E> eventType, Consumer<? super E> handler) {
    DomainEventSubscriber<E> subscriber = new DomainEventSubscriber<E>() {
      @Override
      public Class<E> getEventType() {
        return eventType;
      }

      @Override
      public void handle(E event) {
        handler.accept(event);
      }
    };
    subscribe(subscriber);
    return subscriber;
  }

  public void unsubscribe(DomainEventSubscriber<?> subscriber) {
    subscribers.remove(subscriber);
  }

  @Override
  public void publish(DomainEvent event) {
    for (DomainEventSubscriber<?> subscriber : subscribers) {
      dispatch(subscriber, event);
    }
  }

  private <E extends DomainEvent> void dispatch(DomainEventSubscriber<E> subscriber, DomainEvent event) {
    try {
      Class<E> eventType = subscriber.getEventType();
      if (eventType.isInstance(event)) {
        subscriber.handle(eventType.cast(event));
      }
    } catch (RuntimeException e) {
      logger.error("Subscriber {} failed to handle {}", subscriber, event.getClass().getSimpleName(), e);
    }
  }
}
