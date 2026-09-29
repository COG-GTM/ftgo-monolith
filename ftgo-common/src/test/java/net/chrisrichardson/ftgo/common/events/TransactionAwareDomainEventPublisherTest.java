package net.chrisrichardson.ftgo.common.events;

import net.chrisrichardson.ftgo.common.events.TestEvents.SubEvent;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TransactionAwareDomainEventPublisherTest {

  private final List<DomainEvent> delivered = new ArrayList<>();
  private TransactionAwareDomainEventPublisher publisher;

  @Before
  public void setUp() {
    publisher = new TransactionAwareDomainEventPublisher(delivered::add);
  }

  @After
  public void tearDown() {
    if (TransactionSynchronizationManager.isSynchronizationActive()) {
      TransactionSynchronizationManager.clearSynchronization();
    }
  }

  @Test
  public void shouldPublishImmediatelyWithoutTransaction() {
    SubEvent event = new SubEvent();

    publisher.publish(event);

    assertEquals(Collections.singletonList(event), delivered);
  }

  @Test
  public void shouldDeferPublicationUntilCommit() {
    TransactionSynchronizationManager.initSynchronization();
    SubEvent event = new SubEvent();

    publisher.publish(event);
    assertTrue(delivered.isEmpty());

    commit();
    assertEquals(Collections.singletonList(event), delivered);
  }

  @Test
  public void shouldDiscardEventsOnRollback() {
    TransactionSynchronizationManager.initSynchronization();

    publisher.publish(new SubEvent());
    rollback();

    assertTrue(delivered.isEmpty());
  }

  private void commit() {
    List<TransactionSynchronization> synchronizations = TransactionSynchronizationManager.getSynchronizations();
    synchronizations.forEach(TransactionSynchronization::afterCommit);
    synchronizations.forEach(s -> s.afterCompletion(TransactionSynchronization.STATUS_COMMITTED));
  }

  private void rollback() {
    TransactionSynchronizationManager.getSynchronizations()
            .forEach(s -> s.afterCompletion(TransactionSynchronization.STATUS_ROLLED_BACK));
  }
}
