package net.chrisrichardson.ftgo.eventinfrastructure.domain;

import java.util.List;

public interface DomainEventPublisher {

    void publish(DomainEvent event);

    void publish(List<DomainEvent> events);
}
