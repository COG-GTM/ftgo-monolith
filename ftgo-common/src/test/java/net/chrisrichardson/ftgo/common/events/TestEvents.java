package net.chrisrichardson.ftgo.common.events;

import java.time.Instant;

class TestEvents {

  static class BaseEvent implements DomainEvent {
    private final Instant occurredAt = Instant.now();

    @Override
    public Instant getOccurredAt() {
      return occurredAt;
    }
  }

  static class SubEvent extends BaseEvent {
  }

  static class OtherEvent extends BaseEvent {
  }
}
