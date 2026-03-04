package net.chrisrichardson.ftgo.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import net.chrisrichardson.ftgo.metrics.order.OrderMetrics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Unit tests for {@link OrderMetrics}.
 */
class OrderMetricsTest {

    private MeterRegistry registry;
    private OrderMetrics orderMetrics;

    @BeforeEach
    void setUp() {
        registry = new SimpleMeterRegistry();
        orderMetrics = new OrderMetrics(registry);
    }

    @Test
    void shouldIncrementOrdersCreatedCounter() {
        orderMetrics.orderCreated();
        orderMetrics.orderCreated();

        double count = registry.counter("orders.created", "service", "order-service").count();
        assertEquals(2.0, count);
    }

    @Test
    void shouldIncrementOrdersCancelledCounter() {
        orderMetrics.orderCancelled();

        double count = registry.counter("orders.cancelled", "service", "order-service").count();
        assertEquals(1.0, count);
    }

    @Test
    void shouldRecordOrderTotalValue() {
        orderMetrics.recordOrderValue(1500);
        orderMetrics.recordOrderValue(2500);

        double total = registry.counter("orders.total_value", "service", "order-service").count();
        assertEquals(4000.0, total);
    }

    @Test
    void shouldTrackOrdersByState() {
        orderMetrics.updateOrdersByState("PENDING", 5);
        orderMetrics.updateOrdersByState("APPROVED", 3);

        assertNotNull(registry.find("orders.by_state").tag("state", "PENDING").gauge());
        assertEquals(5.0, registry.find("orders.by_state").tag("state", "PENDING").gauge().value());
        assertEquals(3.0, registry.find("orders.by_state").tag("state", "APPROVED").gauge().value());
    }
}
