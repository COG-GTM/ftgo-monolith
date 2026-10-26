package net.chrisrichardson.ftgo.common.events;

import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Defers delivery to the delegate until the surrounding transaction commits, so subscribers never
 * observe events for changes that were rolled back. Publishes immediately when no transaction is active,
 * or when called by a subscriber while the committing transaction's after-commit callbacks are running
 * (Spring ignores synchronizations registered at that point).
 */
public class TransactionAwareDomainEventPublisher implements DomainEventPublisher {

  private static final ThreadLocal<TransactionSynchronization> committingSynchronization = new ThreadLocal<>();

  private final DomainEventPublisher delegate;

  public TransactionAwareDomainEventPublisher(DomainEventPublisher delegate) {
    this.delegate = delegate;
  }

  @Override
  public void publish(DomainEvent event) {
    if (TransactionSynchronizationManager.isSynchronizationActive() && !isCommitting()) {
      TransactionSynchronizationManager.registerSynchronization(new AfterCommitPublication(event));
    } else {
      delegate.publish(event);
    }
  }

  private boolean isCommitting() {
    TransactionSynchronization committing = committingSynchronization.get();
    return committing != null && TransactionSynchronizationManager.getSynchronizations().contains(committing);
  }

  private class AfterCommitPublication extends TransactionSynchronizationAdapter {

    private final DomainEvent event;

    AfterCommitPublication(DomainEvent event) {
      this.event = event;
    }

    @Override
    public void afterCommit() {
      TransactionSynchronization previous = committingSynchronization.get();
      committingSynchronization.set(this);
      try {
        delegate.publish(event);
      } finally {
        if (previous == null) {
          committingSynchronization.remove();
        } else {
          committingSynchronization.set(previous);
        }
      }
    }
  }
}
