package net.chrisrichardson.ftgo.eventinfrastructure.domain;

import java.time.Instant;

public interface DomainEvent {

    String getAggregateType();

    String getAggregateId();

    String getEventType();

    Instant getOccurredAt();
}
