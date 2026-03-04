package net.chrisrichardson.ftgo.metrics.order;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Custom business metrics for the Order Service.
 *
 * <p>Tracks:
 * <ul>
 *   <li>{@code orders.created} — Counter of orders created</li>
 *   <li>{@code orders.cancelled} — Counter of orders cancelled</li>
 *   <li>{@code orders.total_value} — Counter tracking cumulative order value in cents</li>
 *   <li>{@code orders.by_state} — Gauge showing current count of orders per state</li>
 * </ul>
 */
@Component
public class OrderMetrics {

    private static final Logger log = LoggerFactory.getLogger(OrderMetrics.class);

    private final Counter ordersCreated;
    private final Counter ordersCancelled;
    private final Counter ordersTotalValue;
    private final MeterRegistry registry;
    private final ConcurrentHashMap<String, AtomicInteger> ordersByState = new ConcurrentHashMap<>();

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

        this.ordersTotalValue = Counter.builder("orders.total_value")
                .description("Cumulative order value in cents")
                .baseUnit("cents")
                .tag("service", "order-service")
                .register(registry);

        log.info("Order Service business metrics registered");
    }

    /**
     * Increments the orders created counter.
     */
    public void orderCreated() {
        ordersCreated.increment();
    }

    /**
     * Increments the orders cancelled counter.
     */
    public void orderCancelled() {
        ordersCancelled.increment();
    }

    /**
     * Adds the given value (in cents) to the cumulative order total.
     *
     * @param valueInCents the order value in cents
     */
    public void recordOrderValue(double valueInCents) {
        ordersTotalValue.increment(valueInCents);
    }

    /**
     * Updates the gauge for a specific order state.
     * Call this when an order transitions to a new state.
     *
     * @param state    the order state (e.g., "PENDING", "APPROVED", "CANCELLED")
     * @param newCount the current count of orders in that state
     */
    public void updateOrdersByState(String state, int newCount) {
        ordersByState.computeIfAbsent(state, s -> {
            AtomicInteger gauge = new AtomicInteger(0);
            registry.gauge("orders.by_state", Arrays.asList(
                    Tag.of("state", s),
                    Tag.of("service", "order-service")
            ), gauge);
            return gauge;
        }).set(newCount);
    }
}
