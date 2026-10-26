package net.chrisrichardson.ftgo.common.events;

import net.chrisrichardson.ftgo.common.events.TestEvents.SubEvent;
import org.junit.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class DomainEventsConfigurationTest {

  @Test
  public void shouldCreatePublisherWithoutSubscribers() {
    try (AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(DomainEventsConfiguration.class)) {
      DomainEventPublisher publisher = ctx.getBean(DomainEventPublisher.class);
      assertTrue(publisher instanceof TransactionAwareDomainEventPublisher);
      publisher.publish(new SubEvent());
    }
  }

  @Test
  public void shouldRegisterSubscriberBeansWithPublisher() {
    try (AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(SubscriberConfiguration.class)) {
      ctx.getBean(DomainEventPublisher.class).publish(new SubEvent());

      assertEquals(1, ctx.getBean(RecordingSubscriber.class).received.size());
    }
  }

  @Configuration
  @Import(DomainEventsConfiguration.class)
  static class SubscriberConfiguration {
    @Bean
    public RecordingSubscriber recordingSubscriber() {
      return new RecordingSubscriber();
    }
  }

  static class RecordingSubscriber implements DomainEventSubscriber<SubEvent> {
    final List<SubEvent> received = new ArrayList<>();

    @Override
    public Class<SubEvent> getEventType() {
      return SubEvent.class;
    }

    @Override
    public void handle(SubEvent event) {
      received.add(event);
    }
  }
}
