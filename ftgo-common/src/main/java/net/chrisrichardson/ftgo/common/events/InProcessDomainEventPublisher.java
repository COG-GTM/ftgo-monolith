package net.chrisrichardson.ftgo.common.events;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;

import java.util.List;

/**
 * Delivers domain events synchronously to Spring listeners in the same JVM.
 *
 * <p>Events are dispatched on the caller's thread, inside the caller's transaction. {@code @EventListener}
 * subscribers therefore participate in (and can roll back) that transaction, while
 * {@code @TransactionalEventListener} subscribers run only once it commits.
 */
public class InProcessDomainEventPublisher implements DomainEventPublisher {

  private final Logger logger = LoggerFactory.getLogger(getClass());

  private final ApplicationEventPublisher applicationEventPublisher;

  public InProcessDomainEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
    this.applicationEventPublisher = applicationEventPublisher;
  }

  @Override
  public void publish(String aggregateType, Object aggregateId, List<? extends DomainEvent> domainEvents) {
    for (DomainEvent event : domainEvents) {
      DomainEventEnvelope<DomainEvent> envelope = DomainEventEnvelope.wrap(aggregateType, aggregateId, event);
      logger.debug("Publishing {}", envelope);
      applicationEventPublisher.publishEvent(envelope);
    }
  }
}
