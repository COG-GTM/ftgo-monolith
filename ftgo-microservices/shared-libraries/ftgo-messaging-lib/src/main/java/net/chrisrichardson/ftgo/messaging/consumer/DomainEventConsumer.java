package net.chrisrichardson.ftgo.messaging.consumer;

import net.chrisrichardson.ftgo.messaging.serialization.EventSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;

public abstract class DomainEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(DomainEventConsumer.class);

    private final EventSerializer eventSerializer;

    protected DomainEventConsumer(EventSerializer eventSerializer) {
        this.eventSerializer = eventSerializer;
    }

    protected EventSerializer getEventSerializer() {
        return eventSerializer;
    }

    protected void logEventReceived(String topic, String key, String payload) {
        log.debug("Received event on topic={}, key={}, payload length={}", topic, key, payload.length());
    }
}
