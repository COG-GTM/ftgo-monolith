package com.ftgo.common.metrics.order;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Custom business metrics for the Order Service.
 *
 * <p>Metrics provided:</p>
 * <ul>
 *   <li>{@code orders.created} - Counter of orders created</li>
 *   <li>{@code orders.cancelled} - Counter of orders cancelled</li>
 *   <li>{@code orders.total_value} - Distribution summary of order monetary values</li>
 *   <li>{@code orders.by_state} - Gauge tracking orders in each state</li>
 * </ul>
 *
 * <p>Activate by setting {@code ftgo.metrics.order.enabled=true} in application properties.</p>
 */
@Configuration
@ConditionalOnProperty(name = "ftgo.metrics.order.enabled", havingValue = "true", matchIfMissing = false)
public class OrderMetrics {

    private final MeterRegistry meterRegistry;

    private Counter ordersCreated;
    private Counter ordersCancelled;
    private DistributionSummary ordersTotalValue;

    public OrderMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    @PostConstruct
    public void init() {
        ordersCreated = Counter.builder("orders.created")
                .description("Total number of orders created")
                .tag("service", "order-service")
                .register(meterRegistry);

        ordersCancelled = Counter.builder("orders.cancelled")
                .description("Total number of orders cancelled")
                .tag("service", "order-service")
                .register(meterRegistry);

        ordersTotalValue = DistributionSummary.builder("orders.total_value")
                .description("Distribution of order monetary values in cents")
                .tag("service", "order-service")
                .baseUnit("cents")
                .publishPercentiles(0.5, 0.75, 0.95, 0.99)
                .register(meterRegistry);
    }

    /**
     * Records a new order creation.
     */
    public void recordOrderCreated() {
        ordersCreated.increment();
    }

    /**
     * Records an order cancellation.
     */
    public void recordOrderCancelled() {
        ordersCancelled.increment();
    }

    /**
     * Records an order's monetary value.
     *
     * @param valueInCents the order value in cents
     */
    public void recordOrderValue(double valueInCents) {
        ordersTotalValue.record(valueInCents);
    }

    /**
     * Registers a gauge for tracking orders in a specific state.
     *
     * @param state      the order state name (e.g., "PENDING", "APPROVED", "CANCELLED")
     * @param countHolder an AtomicLong tracking the current count of orders in this state
     */
    public void registerOrdersByState(String state, AtomicLong countHolder) {
        meterRegistry.gauge("orders.by_state",
                Tags.of("state", state, "service", "order-service"),
                countHolder);
    }
}
