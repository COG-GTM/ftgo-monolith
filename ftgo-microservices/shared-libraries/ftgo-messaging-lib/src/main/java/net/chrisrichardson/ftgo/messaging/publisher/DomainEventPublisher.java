package net.chrisrichardson.ftgo.messaging.publisher;

import java.util.List;

public interface DomainEventPublisher {

    void publish(DomainEvent event);

    void publish(List<DomainEvent> events);

    void publish(String topic, DomainEvent event);
}
