package com.ftgo.observability.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.annotation.PostConstruct;
import java.util.concurrent.atomic.AtomicLong;

public class RestaurantMetrics {

    private final MeterRegistry registry;

    private Counter restaurantsCreatedCounter;
    private Counter menuRevisionsCounter;
    private Counter ticketsAcceptedCounter;
    private Counter ticketsRejectedCounter;
    private Timer ticketPreparationTimer;
    private final AtomicLong activeRestaurants = new AtomicLong(0);

    public RestaurantMetrics(MeterRegistry registry) {
        this.registry = registry;
    }

    @PostConstruct
    public void init() {
        restaurantsCreatedCounter = Counter.builder("ftgo.restaurants.created")
                .description("Total number of restaurants created")
                .tag("domain", "restaurant")
                .register(registry);

        menuRevisionsCounter = Counter.builder("ftgo.restaurants.menu.revisions")
                .description("Total number of menu revisions")
                .tag("domain", "restaurant")
                .register(registry);

        ticketsAcceptedCounter = Counter.builder("ftgo.tickets.accepted")
                .description("Total number of tickets accepted by restaurants")
                .tag("domain", "restaurant")
                .register(registry);

        ticketsRejectedCounter = Counter.builder("ftgo.tickets.rejected")
                .description("Total number of tickets rejected by restaurants")
                .tag("domain", "restaurant")
                .register(registry);

        ticketPreparationTimer = Timer.builder("ftgo.tickets.preparation.duration")
                .description("Time taken for ticket preparation")
                .tag("domain", "restaurant")
                .register(registry);

        Gauge.builder("ftgo.restaurants.active", activeRestaurants, AtomicLong::doubleValue)
                .description("Current number of active restaurants")
                .tag("domain", "restaurant")
                .register(registry);
    }

    public void incrementRestaurantsCreated() {
        restaurantsCreatedCounter.increment();
    }

    public void incrementMenuRevisions() {
        menuRevisionsCounter.increment();
    }

    public void incrementTicketsAccepted() {
        ticketsAcceptedCounter.increment();
    }

    public void incrementTicketsRejected() {
        ticketsRejectedCounter.increment();
    }

    public Timer getTicketPreparationTimer() {
        return ticketPreparationTimer;
    }

    public void setActiveRestaurants(long count) {
        activeRestaurants.set(count);
    }
}
