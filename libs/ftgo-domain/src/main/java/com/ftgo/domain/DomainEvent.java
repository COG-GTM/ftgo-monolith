package com.ftgo.domain;

import java.time.Instant;

public interface DomainEvent {

    default Instant occurredOn() {
        return Instant.now();
    }
}
