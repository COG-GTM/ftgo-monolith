package net.chrisrichardson.ftgo.eventinfrastructure.domain;

import java.time.Instant;

public abstract class AbstractDomainEvent implements DomainEvent {

    private final String aggregateType;
    private final String aggregateId;
    private final Instant occurredAt;

    protected AbstractDomainEvent(String aggregateType, String aggregateId) {
        this.aggregateType = aggregateType;
        this.aggregateId = aggregateId;
        this.occurredAt = Instant.now();
    }

    @Override
    public String getAggregateType() {
        return aggregateType;
    }

    @Override
    public String getAggregateId() {
        return aggregateId;
    }

    @Override
    public String getEventType() {
        return this.getClass().getSimpleName();
    }

    @Override
    public Instant getOccurredAt() {
        return occurredAt;
    }
}
