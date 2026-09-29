package net.chrisrichardson.ftgo.common.events;

import net.chrisrichardson.ftgo.common.events.TestEvents.OtherEvent;
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

  @Test
  public void shouldDeliverEventsPublishedBySubscribersDuringCommit() {
    InProcessDomainEventBus bus = new InProcessDomainEventBus();
    TransactionAwareDomainEventPublisher busPublisher = new TransactionAwareDomainEventPublisher(bus);
    OtherEvent followUp = new OtherEvent();
    bus.subscribe(SubEvent.class, e -> busPublisher.publish(followUp));
    bus.subscribe(OtherEvent.class, delivered::add);
    TransactionSynchronizationManager.initSynchronization();

    busPublisher.publish(new SubEvent());
    commit();

    assertEquals(Collections.singletonList(followUp), delivered);
  }

  @Test
  public void shouldDeferEventsPublishedInNewTransactionStartedDuringCommit() {
    InProcessDomainEventBus bus = new InProcessDomainEventBus();
    TransactionAwareDomainEventPublisher busPublisher = new TransactionAwareDomainEventPublisher(bus);
    OtherEvent followUp = new OtherEvent();
    List<List<TransactionSynchronization>> innerSynchronizations = new ArrayList<>();
    bus.subscribe(SubEvent.class, e -> {
      List<TransactionSynchronization> outer = TransactionSynchronizationManager.getSynchronizations();
      TransactionSynchronizationManager.clearSynchronization();
      TransactionSynchronizationManager.initSynchronization();
      busPublisher.publish(followUp);
      assertTrue(delivered.isEmpty());
      innerSynchronizations.add(TransactionSynchronizationManager.getSynchronizations());
      TransactionSynchronizationManager.clearSynchronization();
      TransactionSynchronizationManager.initSynchronization();
      outer.forEach(TransactionSynchronizationManager::registerSynchronization);
    });
    bus.subscribe(OtherEvent.class, delivered::add);
    TransactionSynchronizationManager.initSynchronization();

    busPublisher.publish(new SubEvent());
    commit();
    assertTrue(delivered.isEmpty());

    innerSynchronizations.get(0).forEach(TransactionSynchronization::afterCommit);
    assertEquals(Collections.singletonList(followUp), delivered);
  }

  private void commit() {
    List<TransactionSynchronization> synchronizations = TransactionSynchronizationManager.getSynchronizations();
    synchronizations.forEach(TransactionSynchronization::afterCommit);
    synchronizations.forEach(s -> s.afterCompletion(TransactionSynchronization.STATUS_COMMITTED));
    TransactionSynchronizationManager.clearSynchronization();
  }

  private void rollback() {
    TransactionSynchronizationManager.getSynchronizations()
            .forEach(s -> s.afterCompletion(TransactionSynchronization.STATUS_ROLLED_BACK));
  }
}
