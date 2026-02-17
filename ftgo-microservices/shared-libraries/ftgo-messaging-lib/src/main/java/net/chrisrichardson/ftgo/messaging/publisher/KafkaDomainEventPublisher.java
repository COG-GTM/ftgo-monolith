package net.chrisrichardson.ftgo.messaging.publisher;

import net.chrisrichardson.ftgo.messaging.serialization.EventSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.List;

public class KafkaDomainEventPublisher implements DomainEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(KafkaDomainEventPublisher.class);

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final EventSerializer eventSerializer;

    public KafkaDomainEventPublisher(KafkaTemplate<String, String> kafkaTemplate,
                                     EventSerializer eventSerializer) {
        this.kafkaTemplate = kafkaTemplate;
        this.eventSerializer = eventSerializer;
    }

    @Override
    public void publish(DomainEvent event) {
        String topic = event.getAggregateType();
        publish(topic, event);
    }

    @Override
    public void publish(List<DomainEvent> events) {
        events.forEach(this::publish);
    }

    @Override
    public void publish(String topic, DomainEvent event) {
        String payload = eventSerializer.serialize(event);
        log.debug("Publishing event {} to topic {}", event.getEventType(), topic);
        kafkaTemplate.send(topic, event.getAggregateId(), payload);
    }
}
