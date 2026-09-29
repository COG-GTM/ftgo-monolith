package net.chrisrichardson.ftgo.common.events;

import net.chrisrichardson.ftgo.common.events.TestEvents.BaseEvent;
import net.chrisrichardson.ftgo.common.events.TestEvents.OtherEvent;
import net.chrisrichardson.ftgo.common.events.TestEvents.SubEvent;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class InProcessDomainEventBusTest {

  private InProcessDomainEventBus bus;

  @Before
  public void setUp() {
    bus = new InProcessDomainEventBus();
  }

  @Test
  public void shouldDeliverEventToSubscriberOfExactType() {
    List<SubEvent> received = new ArrayList<>();
    bus.subscribe(SubEvent.class, received::add);

    SubEvent event = new SubEvent();
    bus.publish(event);

    assertEquals(Collections.singletonList(event), received);
  }

  @Test
  public void shouldDeliverEventToSubscriberOfSupertype() {
    List<DomainEvent> received = new ArrayList<>();
    bus.subscribe(DomainEvent.class, received::add);
    bus.subscribe(BaseEvent.class, received::add);

    SubEvent event = new SubEvent();
    bus.publish(event);

    assertEquals(Arrays.asList(event, event), received);
  }

  @Test
  public void shouldNotDeliverEventToSubscriberOfUnrelatedType() {
    List<OtherEvent> received = new ArrayList<>();
    bus.subscribe(OtherEvent.class, received::add);

    bus.publish(new SubEvent());

    assertTrue(received.isEmpty());
  }

  @Test
  public void shouldPublishWithoutSubscribers() {
    bus.publish(new SubEvent());
  }

  @Test
  public void shouldDeliverEventsInPublicationOrder() {
    List<DomainEvent> received = new ArrayList<>();
    bus.subscribe(DomainEvent.class, received::add);

    SubEvent first = new SubEvent();
    OtherEvent second = new OtherEvent();
    bus.publish(Arrays.asList(first, second));

    assertEquals(Arrays.asList(first, second), received);
  }

  @Test
  public void shouldIsolateFailingSubscriber() {
    List<String> calls = new ArrayList<>();
    bus.subscribe(SubEvent.class, e -> {
      calls.add("failing");
      throw new RuntimeException("boom");
    });
    bus.subscribe(SubEvent.class, e -> calls.add("healthy"));

    bus.publish(new SubEvent());

    assertEquals(Arrays.asList("failing", "healthy"), calls);
  }

  @Test
  public void shouldRegisterSubscribersPassedToConstructor() {
    List<SubEvent> received = new ArrayList<>();
    DomainEventSubscriber<SubEvent> subscriber = new DomainEventSubscriber<SubEvent>() {
      @Override
      public Class<SubEvent> getEventType() {
        return SubEvent.class;
      }

      @Override
      public void handle(SubEvent event) {
        received.add(event);
      }
    };

    new InProcessDomainEventBus(Collections.singletonList(subscriber)).publish(new SubEvent());

    assertEquals(1, received.size());
  }

  @Test
  public void shouldStopDeliveringAfterUnsubscribe() {
    List<SubEvent> received = new ArrayList<>();
    DomainEventSubscriber<SubEvent> subscriber = bus.subscribe(SubEvent.class, received::add);

    bus.unsubscribe(subscriber);
    bus.publish(new SubEvent());

    assertTrue(received.isEmpty());
  }
}
