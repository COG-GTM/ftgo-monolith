package net.chrisrichardson.ftgo.eventinfrastructure.outbox;

import net.chrisrichardson.ftgo.eventinfrastructure.kafka.KafkaEventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public class OutboxRelay {

    private static final Logger logger = LoggerFactory.getLogger(OutboxRelay.class);

    private final OutboxRepository outboxRepository;
    private final KafkaEventPublisher kafkaEventPublisher;

    public OutboxRelay(OutboxRepository outboxRepository, KafkaEventPublisher kafkaEventPublisher) {
        this.outboxRepository = outboxRepository;
        this.kafkaEventPublisher = kafkaEventPublisher;
    }

    @Scheduled(fixedDelayString = "${ftgo.outbox.relay.interval-ms:1000}")
    @Transactional
    public void pollAndPublish() {
        List<OutboxEvent> unpublishedEvents = outboxRepository.findUnpublishedEvents();

        if (!unpublishedEvents.isEmpty()) {
            logger.info("Found {} unpublished events in outbox", unpublishedEvents.size());
        }

        for (OutboxEvent event : unpublishedEvents) {
            try {
                String topic = resolveTopicName(event.getAggregateType());
                kafkaEventPublisher.publish(topic, event.getAggregateId(), event.getPayload());
                event.markPublished();
                outboxRepository.save(event);
                logger.debug("Published outbox event: id={}, type={}", event.getId(), event.getEventType());
            } catch (Exception e) {
                logger.error("Failed to publish outbox event: id={}, type={}",
                        event.getId(), event.getEventType(), e);
                break;
            }
        }
    }

    private String resolveTopicName(String aggregateType) {
        return "ftgo." + aggregateType.toLowerCase() + ".events";
    }
}
