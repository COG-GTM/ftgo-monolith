package com.ftgo.domain;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ResultWithEvents<T> {

    private final T result;
    private final List<DomainEvent> events;

    public ResultWithEvents(T result, List<DomainEvent> events) {
        this.result = result;
        this.events = Collections.unmodifiableList(events);
    }

    public ResultWithEvents(T result, DomainEvent... events) {
        this(result, Arrays.asList(events));
    }

    public T getResult() {
        return result;
    }

    public List<DomainEvent> getEvents() {
        return events;
    }
}
