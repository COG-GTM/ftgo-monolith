package net.chrisrichardson.ftgo.common.events;

import java.time.Instant;

/**
 * A fact about something that happened in the domain.
 * Implementations must be immutable so they can be safely dispatched to
 * in-process subscribers today and serialized onto a message broker later.
 */
public interface DomainEvent {

  Instant getOccurredAt();
}
