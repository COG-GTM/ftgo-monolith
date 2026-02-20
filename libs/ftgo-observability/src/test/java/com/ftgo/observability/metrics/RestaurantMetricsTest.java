package com.ftgo.observability.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RestaurantMetricsTest {

    private MeterRegistry registry;
    private RestaurantMetrics restaurantMetrics;

    @BeforeEach
    void setUp() {
        registry = new SimpleMeterRegistry();
        restaurantMetrics = new RestaurantMetrics(registry);
        restaurantMetrics.init();
    }

    @Test
    void shouldIncrementRestaurantsCreatedCounter() {
        restaurantMetrics.incrementRestaurantsCreated();

        assertThat(registry.counter("ftgo.restaurants.created", "domain", "restaurant").count()).isEqualTo(1.0);
    }

    @Test
    void shouldIncrementMenuRevisionsCounter() {
        restaurantMetrics.incrementMenuRevisions();
        restaurantMetrics.incrementMenuRevisions();

        assertThat(registry.counter("ftgo.restaurants.menu.revisions", "domain", "restaurant").count()).isEqualTo(2.0);
    }

    @Test
    void shouldIncrementTicketsAcceptedCounter() {
        restaurantMetrics.incrementTicketsAccepted();

        assertThat(registry.counter("ftgo.tickets.accepted", "domain", "restaurant").count()).isEqualTo(1.0);
    }

    @Test
    void shouldTrackActiveRestaurantsGauge() {
        restaurantMetrics.setActiveRestaurants(100);

        assertThat(registry.get("ftgo.restaurants.active").gauge().value()).isEqualTo(100.0);
    }

    @Test
    void shouldProvideTicketPreparationTimer() {
        assertThat(restaurantMetrics.getTicketPreparationTimer()).isNotNull();
    }
}
