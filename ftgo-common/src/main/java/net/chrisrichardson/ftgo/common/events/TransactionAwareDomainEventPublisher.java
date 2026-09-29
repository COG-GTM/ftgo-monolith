package net.chrisrichardson.ftgo.common.events;

import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Defers delivery to the delegate until the surrounding transaction commits, so subscribers never
 * observe events for changes that were rolled back. Publishes immediately when no transaction is active.
 */
public class TransactionAwareDomainEventPublisher implements DomainEventPublisher {

  private final DomainEventPublisher delegate;

  public TransactionAwareDomainEventPublisher(DomainEventPublisher delegate) {
    this.delegate = delegate;
  }

  @Override
  public void publish(DomainEvent event) {
    if (TransactionSynchronizationManager.isSynchronizationActive()) {
      TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
        @Override
        public void afterCommit() {
          delegate.publish(event);
        }
      });
    } else {
      delegate.publish(event);
    }
  }
}
