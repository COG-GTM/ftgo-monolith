package com.ftgo.observability.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CourierMetricsTest {

    private MeterRegistry registry;
    private CourierMetrics courierMetrics;

    @BeforeEach
    void setUp() {
        registry = new SimpleMeterRegistry();
        courierMetrics = new CourierMetrics(registry);
        courierMetrics.init();
    }

    @Test
    void shouldIncrementDeliveriesCompletedCounter() {
        courierMetrics.incrementDeliveriesCompleted();

        assertThat(registry.counter("ftgo.deliveries.completed", "domain", "courier").count()).isEqualTo(1.0);
    }

    @Test
    void shouldIncrementDeliveriesFailedCounter() {
        courierMetrics.incrementDeliveriesFailed();

        assertThat(registry.counter("ftgo.deliveries.failed", "domain", "courier").count()).isEqualTo(1.0);
    }

    @Test
    void shouldTrackAvailableCouriersGauge() {
        courierMetrics.setAvailableCouriers(15);

        assertThat(registry.get("ftgo.couriers.available").gauge().value()).isEqualTo(15.0);
    }

    @Test
    void shouldRecordDeliveryDistance() {
        courierMetrics.recordDeliveryDistance(5.5);
        courierMetrics.recordDeliveryDistance(3.2);

        assertThat(registry.summary("ftgo.deliveries.distance", "domain", "courier").count()).isEqualTo(2);
    }

    @Test
    void shouldProvideDeliveryDurationTimer() {
        assertThat(courierMetrics.getDeliveryDurationTimer()).isNotNull();
    }
}
