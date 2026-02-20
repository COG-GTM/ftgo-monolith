package com.ftgo.domain;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DomainEventPublisherTest {

    @BeforeEach
    void setUp() {
        DomainEventPublisher.clear();
    }

    @AfterEach
    void tearDown() {
        DomainEventPublisher.clear();
    }

    @Test
    void publishInvokesSubscribedHandler() {
        List<DomainEvent> received = new ArrayList<>();
        DomainEventPublisher.subscribe(TestOrderEvent.class, received::add);

        TestOrderEvent event = new TestOrderEvent("order-1");
        DomainEventPublisher.publish(event);

        assertThat(received).hasSize(1);
    }

    @Test
    void clearRemovesAllHandlers() {
        List<DomainEvent> received = new ArrayList<>();
        DomainEventPublisher.subscribe(TestOrderEvent.class, received::add);

        DomainEventPublisher.clear();
        DomainEventPublisher.publish(new TestOrderEvent("order-2"));

        assertThat(received).isEmpty();
    }

    @Test
    void multipleSubscribersReceiveEvent() {
        List<String> log1 = new ArrayList<>();
        List<String> log2 = new ArrayList<>();

        DomainEventPublisher.subscribe(TestOrderEvent.class, e -> log1.add("h1"));
        DomainEventPublisher.subscribe(TestOrderEvent.class, e -> log2.add("h2"));

        DomainEventPublisher.publish(new TestOrderEvent("order-3"));

        assertThat(log1).hasSize(1);
        assertThat(log2).hasSize(1);
    }

    @Test
    void domainEventHasOccurredOn() {
        TestOrderEvent event = new TestOrderEvent("order-4");
        assertThat(event.occurredOn()).isNotNull();
    }

    private static class TestOrderEvent implements DomainEvent {
        private final String orderId;

        TestOrderEvent(String orderId) {
            this.orderId = orderId;
        }
    }
}
