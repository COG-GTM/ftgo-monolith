package com.ftgo.restaurant.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.Gauge;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Custom business metrics for the FTGO Restaurant Service.
 *
 * <p>Registers and exposes the following metrics:
 * <ul>
 *   <li>{@code ftgo.restaurants.created} - Counter of restaurants created</li>
 *   <li>{@code ftgo.restaurants.updated} - Counter of restaurant updates</li>
 *   <li>{@code ftgo.tickets.accepted} - Counter of tickets accepted by restaurants</li>
 *   <li>{@code ftgo.tickets.preparing} - Gauge of tickets currently being prepared</li>
 *   <li>{@code ftgo.tickets.ready} - Counter of tickets marked ready for pickup</li>
 *   <li>{@code ftgo.tickets.cancelled} - Counter of tickets cancelled</li>
 *   <li>{@code ftgo.ticket.preparation.time} - Timer for ticket preparation duration</li>
 * </ul>
 */
@Component
public class RestaurantMetrics {

    private final Counter restaurantsCreated;
    private final Counter restaurantsUpdated;
    private final Counter ticketsAccepted;
    private final AtomicInteger ticketsPreparing;
    private final Counter ticketsReady;
    private final Counter ticketsCancelled;
    private final Timer ticketPreparationTime;

    public RestaurantMetrics(MeterRegistry registry) {
        this.restaurantsCreated = Counter.builder("ftgo.restaurants.created")
                .description("Total number of restaurants created")
                .tag("service", "restaurant-service")
                .register(registry);

        this.restaurantsUpdated = Counter.builder("ftgo.restaurants.updated")
                .description("Total number of restaurant updates")
                .tag("service", "restaurant-service")
                .register(registry);

        this.ticketsAccepted = Counter.builder("ftgo.tickets.accepted")
                .description("Total number of tickets accepted by restaurants")
                .tag("service", "restaurant-service")
                .register(registry);

        this.ticketsPreparing = new AtomicInteger(0);
        Gauge.builder("ftgo.tickets.preparing", ticketsPreparing, AtomicInteger::get)
                .description("Number of tickets currently being prepared")
                .tag("service", "restaurant-service")
                .register(registry);

        this.ticketsReady = Counter.builder("ftgo.tickets.ready")
                .description("Total number of tickets marked ready for pickup")
                .tag("service", "restaurant-service")
                .register(registry);

        this.ticketsCancelled = Counter.builder("ftgo.tickets.cancelled")
                .description("Total number of tickets cancelled")
                .tag("service", "restaurant-service")
                .register(registry);

        this.ticketPreparationTime = Timer.builder("ftgo.ticket.preparation.time")
                .description("Time taken from ticket acceptance to ready for pickup")
                .tag("service", "restaurant-service")
                .register(registry);
    }

    public void incrementRestaurantsCreated() {
        restaurantsCreated.increment();
    }

    public void incrementRestaurantsUpdated() {
        restaurantsUpdated.increment();
    }

    public void incrementTicketsAccepted() {
        ticketsAccepted.increment();
    }

    public void incrementTicketsPreparing() {
        ticketsPreparing.incrementAndGet();
    }

    public void decrementTicketsPreparing() {
        ticketsPreparing.decrementAndGet();
    }

    public void incrementTicketsReady() {
        ticketsReady.increment();
    }

    public void incrementTicketsCancelled() {
        ticketsCancelled.increment();
    }

    public Timer.Sample startPreparationTimer() {
        return Timer.start();
    }

    public void stopPreparationTimer(Timer.Sample sample) {
        sample.stop(ticketPreparationTime);
    }
}
