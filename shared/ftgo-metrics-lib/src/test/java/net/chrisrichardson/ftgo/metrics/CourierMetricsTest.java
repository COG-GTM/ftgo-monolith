package net.chrisrichardson.ftgo.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import net.chrisrichardson.ftgo.metrics.courier.CourierMetrics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.micrometer.core.instrument.Timer;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Unit tests for {@link CourierMetrics}.
 */
class CourierMetricsTest {

    private MeterRegistry registry;
    private CourierMetrics courierMetrics;

    @BeforeEach
    void setUp() {
        registry = new SimpleMeterRegistry();
        courierMetrics = new CourierMetrics(registry);
    }

    @Test
    void shouldIncrementDeliveriesAssignedCounter() {
        courierMetrics.deliveryAssigned();
        courierMetrics.deliveryAssigned();

        double count = registry.counter("deliveries.assigned", "service", "courier-service").count();
        assertEquals(2.0, count);
    }

    @Test
    void shouldIncrementDeliveriesCompletedCounter() {
        courierMetrics.deliveryCompleted();

        double count = registry.counter("deliveries.completed", "service", "courier-service").count();
        assertEquals(1.0, count);
    }

    @Test
    void shouldRecordDeliveryTime() {
        courierMetrics.recordDeliveryTime(Duration.ofMinutes(30));
        courierMetrics.recordDeliveryTime(Duration.ofMinutes(45));

        Timer timer = registry.find("deliveries.average_time").tag("service", "courier-service").timer();
        assertNotNull(timer);
        assertEquals(2, timer.count());
    }
}
