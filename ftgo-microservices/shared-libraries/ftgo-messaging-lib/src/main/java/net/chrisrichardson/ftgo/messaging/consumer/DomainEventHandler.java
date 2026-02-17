package net.chrisrichardson.ftgo.messaging.consumer;

import net.chrisrichardson.ftgo.messaging.publisher.DomainEvent;

@FunctionalInterface
public interface DomainEventHandler<T extends DomainEvent> {

    void handle(T event);
}
