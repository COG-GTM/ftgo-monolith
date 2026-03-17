package net.chrisrichardson.ftgo.observability.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Custom business metrics for the Order Service.
 * Tracks order lifecycle events, monetary values, and state distribution.
 * Only activated when spring.application.name is set to order-service.
 */
@Component
@ConditionalOnProperty(name = "spring.application.name", havingValue = "order-service")
public class OrderMetrics {

    private final Counter ordersCreated;
    private final Counter ordersCancelled;
    private final DistributionSummary ordersTotalValue;
    private final MeterRegistry registry;
    private final ConcurrentHashMap<String, AtomicInteger> orderStateGauges = new ConcurrentHashMap<>();

    public OrderMetrics(MeterRegistry registry) {
        this.registry = registry;

        this.ordersCreated = Counter.builder("orders.created")
                .description("Total number of orders created")
                .tag("service", "order-service")
                .register(registry);

        this.ordersCancelled = Counter.builder("orders.cancelled")
                .description("Total number of orders cancelled")
                .tag("service", "order-service")
                .register(registry);

        this.ordersTotalValue = DistributionSummary.builder("orders.total_value")
                .description("Distribution of order values in cents")
                .tag("service", "order-service")
                .baseUnit("cents")
                .register(registry);
    }

    /**
     * Record a new order creation.
     */
    public void recordOrderCreated() {
        ordersCreated.increment();
    }

    /**
     * Record an order cancellation.
     */
    public void recordOrderCancelled() {
        ordersCancelled.increment();
    }

    /**
     * Record the total value of an order.
     *
     * @param valueInCents the order value in cents
     */
    public void recordOrderValue(long valueInCents) {
        ordersTotalValue.record(valueInCents);
    }

    /**
     * Update the gauge tracking orders in a specific state.
     * Uses AtomicInteger to ensure the gauge tracks live value changes.
     *
     * @param state the order state
     * @param count the current count of orders in that state
     */
    public void recordOrdersByState(String state, int count) {
        orderStateGauges.computeIfAbsent(state, s -> {
            AtomicInteger gauge = new AtomicInteger(0);
            registry.gauge("orders.by_state",
                    Tags.of("state", s, "service", "order-service"),
                    gauge);
            return gauge;
        }).set(count);
    }
}
