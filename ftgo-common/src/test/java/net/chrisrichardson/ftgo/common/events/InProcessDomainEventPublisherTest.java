package net.chrisrichardson.ftgo.common.events;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.event.EventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class InProcessDomainEventPublisherTest {

  static class Animal {
  }

  static abstract class AnimalEvent implements DomainEvent {
  }

  static class AnimalBornEvent extends AnimalEvent {
  }

  static class AnimalFedEvent extends AnimalEvent {
  }

  static class RecordingListeners {
    final List<DomainEventEnvelope<AnimalBornEvent>> bornEvents = new ArrayList<>();
    final List<DomainEventEnvelope<? extends AnimalEvent>> allAnimalEvents = new ArrayList<>();

    @EventListener
    public void onBorn(DomainEventEnvelope<AnimalBornEvent> envelope) {
      bornEvents.add(envelope);
    }

    @EventListener
    public void onAnyAnimalEvent(DomainEventEnvelope<? extends AnimalEvent> envelope) {
      allAnimalEvents.add(envelope);
    }
  }

  @Configuration
  @Import(DomainEventsConfiguration.class)
  static class TestConfiguration {
    @Bean
    public RecordingListeners recordingListeners() {
      return new RecordingListeners();
    }
  }

  private AnnotationConfigApplicationContext context;
  private DomainEventPublisher publisher;
  private RecordingListeners listeners;

  @Before
  public void setUp() {
    context = new AnnotationConfigApplicationContext(TestConfiguration.class);
    publisher = context.getBean(DomainEventPublisher.class);
    listeners = context.getBean(RecordingListeners.class);
  }

  @After
  public void tearDown() {
    context.close();
  }

  @Test
  public void shouldDeliverEachEventWrappedInAnEnvelope() {
    AnimalBornEvent born = new AnimalBornEvent();
    AnimalFedEvent fed = new AnimalFedEvent();

    publisher.publish(Animal.class, 42L, Arrays.asList(born, fed));

    assertEquals(2, listeners.allAnimalEvents.size());
    DomainEventEnvelope<? extends AnimalEvent> first = listeners.allAnimalEvents.get(0);
    DomainEventEnvelope<? extends AnimalEvent> second = listeners.allAnimalEvents.get(1);

    assertSame(born, first.getEvent());
    assertSame(fed, second.getEvent());
    assertEquals(Animal.class.getName(), first.getAggregateType());
    assertEquals("42", first.getAggregateId());
    assertEquals(AnimalBornEvent.class.getName(), first.getEventType());
    assertNotNull(first.getPublishedAt());
    assertNotEquals(first.getEventId(), second.getEventId());
  }

  @Test
  public void shouldRouteEnvelopesByEventType() {
    publisher.publish(Animal.class, 1L, Arrays.asList(new AnimalFedEvent(), new AnimalBornEvent()));

    assertEquals(1, listeners.bornEvents.size());
    assertTrue(listeners.bornEvents.get(0).getEvent() instanceof AnimalBornEvent);
  }

  @Test
  public void shouldPublishNothingForEmptyEventList() {
    publisher.publish(Animal.class, 1L, Collections.emptyList());

    assertTrue(listeners.allAnimalEvents.isEmpty());
  }
}
