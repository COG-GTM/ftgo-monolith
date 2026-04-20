package net.chrisrichardson.ftgo.messaging.publisher;

import java.time.Instant;
import java.util.UUID;

public abstract class DomainEvent {

    private final String eventId;
    private final Instant timestamp;
    private final String aggregateType;
    private final String aggregateId;

    protected DomainEvent(String aggregateType, String aggregateId) {
        this.eventId = UUID.randomUUID().toString();
        this.timestamp = Instant.now();
        this.aggregateType = aggregateType;
        this.aggregateId = aggregateId;
    }

    public String getEventId() {
        return eventId;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public String getAggregateType() {
        return aggregateType;
    }

    public String getAggregateId() {
        return aggregateId;
    }

    public String getEventType() {
        return getClass().getSimpleName();
    }
}
