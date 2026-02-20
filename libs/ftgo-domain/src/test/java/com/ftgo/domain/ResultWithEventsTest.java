package com.ftgo.domain;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ResultWithEventsTest {

    @Test
    void createdWithResultAndEventList() {
        DomainEvent event = new TestEvent("created");
        ResultWithEvents<String> rwe = new ResultWithEvents<>("result", Collections.singletonList(event));

        assertThat(rwe.getResult()).isEqualTo("result");
        assertThat(rwe.getEvents()).hasSize(1);
    }

    @Test
    void createdWithVarargs() {
        DomainEvent event1 = new TestEvent("e1");
        DomainEvent event2 = new TestEvent("e2");
        ResultWithEvents<Integer> rwe = new ResultWithEvents<>(42, event1, event2);

        assertThat(rwe.getResult()).isEqualTo(42);
        assertThat(rwe.getEvents()).hasSize(2);
    }

    @Test
    void eventsListIsUnmodifiable() {
        DomainEvent event = new TestEvent("test");
        ResultWithEvents<String> rwe = new ResultWithEvents<>("value", Collections.singletonList(event));

        List<DomainEvent> events = rwe.getEvents();
        org.junit.jupiter.api.Assertions.assertThrows(UnsupportedOperationException.class, () -> events.add(new TestEvent("new")));
    }

    @Test
    void emptyEventList() {
        ResultWithEvents<String> rwe = new ResultWithEvents<>("value", Collections.emptyList());

        assertThat(rwe.getResult()).isEqualTo("value");
        assertThat(rwe.getEvents()).isEmpty();
    }

    @Test
    void multipleEvents() {
        List<DomainEvent> events = Arrays.asList(
                new TestEvent("first"),
                new TestEvent("second"),
                new TestEvent("third")
        );
        ResultWithEvents<String> rwe = new ResultWithEvents<>("result", events);

        assertThat(rwe.getEvents()).hasSize(3);
    }

    private static class TestEvent implements DomainEvent {
        private final String name;

        TestEvent(String name) {
            this.name = name;
        }
    }
}
