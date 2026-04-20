package net.chrisrichardson.ftgo.eventinfrastructure.outbox;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.chrisrichardson.ftgo.eventinfrastructure.domain.DomainEvent;
import net.chrisrichardson.ftgo.eventinfrastructure.domain.DomainEventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional
public class OutboxEventPublisher implements DomainEventPublisher {

    private static final Logger logger = LoggerFactory.getLogger(OutboxEventPublisher.class);

    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;

    public OutboxEventPublisher(OutboxRepository outboxRepository, ObjectMapper objectMapper) {
        this.outboxRepository = outboxRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public void publish(DomainEvent event) {
        try {
            String payload = objectMapper.writeValueAsString(event);
            OutboxEvent outboxEvent = new OutboxEvent(
                    event.getAggregateType(),
                    event.getAggregateId(),
                    event.getEventType(),
                    payload
            );
            outboxRepository.save(outboxEvent);
            logger.debug("Stored event in outbox: type={}, aggregateId={}",
                    event.getEventType(), event.getAggregateId());
        } catch (JsonProcessingException e) {
            logger.error("Failed to serialize domain event: {}", event.getEventType(), e);
            throw new RuntimeException("Failed to serialize domain event", e);
        }
    }

    @Override
    public void publish(List<DomainEvent> events) {
        events.forEach(this::publish);
    }
}
