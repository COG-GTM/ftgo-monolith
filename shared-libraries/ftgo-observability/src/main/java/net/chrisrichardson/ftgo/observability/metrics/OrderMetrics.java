package net.chrisrichardson.ftgo.observability.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Custom business metrics for the Order Service.
 * Tracks order creation, cancellation, total value, and orders by state.
 */
public class OrderMetrics {

    private final Counter ordersCreated;
    private final Counter ordersCancelled;
    private final DistributionSummary ordersTotalValue;
    private final MeterRegistry registry;

    public OrderMetrics(MeterRegistry registry) {
        this.registry = registry;

        this.ordersCreated = Counter.builder("orders.created")
                .description("Total number of orders created")
                .register(registry);

        this.ordersCancelled = Counter.builder("orders.cancelled")
                .description("Total number of orders cancelled")
                .register(registry);

        this.ordersTotalValue = DistributionSummary.builder("orders.total_value")
                .description("Distribution of order values")
                .baseUnit("currency")
                .register(registry);
    }

    public void recordOrderCreated() {
        ordersCreated.increment();
    }

    public void recordOrderCancelled() {
        ordersCancelled.increment();
    }

    public void recordOrderValue(double value) {
        ordersTotalValue.record(value);
    }

    /**
     * Updates the gauge for orders in a given state.
     * Call this method to set the current count for a specific order state.
     */
    public void setOrdersByState(String state, AtomicInteger gauge) {
        registry.gauge("orders.by_state", Tags.of("state", state), gauge);
    }
}
