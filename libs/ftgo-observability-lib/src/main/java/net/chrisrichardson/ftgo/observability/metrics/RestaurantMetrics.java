package net.chrisrichardson.ftgo.observability.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

public class RestaurantMetrics {

    private final Counter restaurantsCreated;
    private final Counter menuUpdates;
    private final Counter ticketsCreated;
    private final Counter ticketsAccepted;
    private final Counter ticketsRejected;
    private final Timer ticketPreparationTime;

    public RestaurantMetrics(MeterRegistry registry) {
        this.restaurantsCreated = Counter.builder(FtgoMetricsConstants.PREFIX_RESTAURANT + ".created.total")
                .description("Total number of restaurants created")
                .tag(FtgoMetricsConstants.TAG_SERVICE, "restaurant-service")
                .register(registry);

        this.menuUpdates = Counter.builder(FtgoMetricsConstants.PREFIX_RESTAURANT + ".menu.updates.total")
                .description("Total number of restaurant menu updates")
                .tag(FtgoMetricsConstants.TAG_SERVICE, "restaurant-service")
                .register(registry);

        this.ticketsCreated = Counter.builder(FtgoMetricsConstants.PREFIX_RESTAURANT + ".tickets.created.total")
                .description("Total number of tickets created")
                .tag(FtgoMetricsConstants.TAG_SERVICE, "restaurant-service")
                .register(registry);

        this.ticketsAccepted = Counter.builder(FtgoMetricsConstants.PREFIX_RESTAURANT + ".tickets.accepted.total")
                .description("Total number of tickets accepted by restaurant")
                .tag(FtgoMetricsConstants.TAG_SERVICE, "restaurant-service")
                .register(registry);

        this.ticketsRejected = Counter.builder(FtgoMetricsConstants.PREFIX_RESTAURANT + ".tickets.rejected.total")
                .description("Total number of tickets rejected by restaurant")
                .tag(FtgoMetricsConstants.TAG_SERVICE, "restaurant-service")
                .register(registry);

        this.ticketPreparationTime = Timer.builder(FtgoMetricsConstants.PREFIX_RESTAURANT + ".ticket.preparation.duration")
                .description("Time taken to prepare a ticket")
                .tag(FtgoMetricsConstants.TAG_SERVICE, "restaurant-service")
                .register(registry);
    }

    public Counter getRestaurantsCreated() {
        return restaurantsCreated;
    }

    public Counter getMenuUpdates() {
        return menuUpdates;
    }

    public Counter getTicketsCreated() {
        return ticketsCreated;
    }

    public Counter getTicketsAccepted() {
        return ticketsAccepted;
    }

    public Counter getTicketsRejected() {
        return ticketsRejected;
    }

    public Timer getTicketPreparationTime() {
        return ticketPreparationTime;
    }
}
