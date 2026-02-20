package com.ftgo.domain;

import java.util.ArrayList;
import java.util.List;

public class DomainEventPublisher {

    private static final ThreadLocal<List<DomainEventHandler<?>>> handlers = ThreadLocal.withInitial(ArrayList::new);

    private DomainEventPublisher() {
    }

    public static <T extends DomainEvent> void subscribe(Class<T> eventType, DomainEventHandler<T> handler) {
        handlers.get().add(handler);
    }

    @SuppressWarnings("unchecked")
    public static void publish(DomainEvent event) {
        List<DomainEventHandler<?>> registeredHandlers = handlers.get();
        for (DomainEventHandler<?> handler : registeredHandlers) {
            ((DomainEventHandler<DomainEvent>) handler).handle(event);
        }
    }

    public static void clear() {
        handlers.get().clear();
    }

    @FunctionalInterface
    public interface DomainEventHandler<T extends DomainEvent> {
        void handle(T event);
    }
}
