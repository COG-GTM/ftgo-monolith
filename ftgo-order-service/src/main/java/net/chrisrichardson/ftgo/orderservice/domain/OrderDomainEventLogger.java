package net.chrisrichardson.ftgo.orderservice.domain;

import net.chrisrichardson.ftgo.common.events.DomainEventEnvelope;
import net.chrisrichardson.ftgo.domain.events.OrderDomainEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Example subscriber: logs every order domain event once the transaction that produced it has committed.
 */
public class OrderDomainEventLogger {

  private final Logger logger = LoggerFactory.getLogger(getClass());

  @TransactionalEventListener(fallbackExecution = true)
  public void onOrderEvent(DomainEventEnvelope<? extends OrderDomainEvent> envelope) {
    logger.info("Order {} event {}: {}", envelope.getAggregateId(), envelope.getEvent().getClass().getSimpleName(), envelope.getEvent());
  }
}
