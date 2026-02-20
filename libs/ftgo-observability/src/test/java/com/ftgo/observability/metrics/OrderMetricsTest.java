package com.ftgo.observability.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OrderMetricsTest {

    private MeterRegistry registry;
    private OrderMetrics orderMetrics;

    @BeforeEach
    void setUp() {
        registry = new SimpleMeterRegistry();
        orderMetrics = new OrderMetrics(registry);
        orderMetrics.init();
    }

    @Test
    void shouldIncrementOrdersCreatedCounter() {
        orderMetrics.incrementOrdersCreated();
        orderMetrics.incrementOrdersCreated();

        assertThat(registry.counter("ftgo.orders.created", "domain", "order").count()).isEqualTo(2.0);
    }

    @Test
    void shouldIncrementOrdersApprovedCounter() {
        orderMetrics.incrementOrdersApproved();

        assertThat(registry.counter("ftgo.orders.approved", "domain", "order").count()).isEqualTo(1.0);
    }

    @Test
    void shouldIncrementOrdersRejectedCounter() {
        orderMetrics.incrementOrdersRejected();

        assertThat(registry.counter("ftgo.orders.rejected", "domain", "order").count()).isEqualTo(1.0);
    }

    @Test
    void shouldIncrementOrdersCancelledCounter() {
        orderMetrics.incrementOrdersCancelled();

        assertThat(registry.counter("ftgo.orders.cancelled", "domain", "order").count()).isEqualTo(1.0);
    }

    @Test
    void shouldProvideOrderFulfillmentTimer() {
        assertThat(orderMetrics.getOrderFulfillmentTimer()).isNotNull();
        assertThat(registry.timer("ftgo.orders.fulfillment.duration", "domain", "order")).isNotNull();
    }

    @Test
    void shouldProvideOrderApprovalTimer() {
        assertThat(orderMetrics.getOrderApprovalTimer()).isNotNull();
        assertThat(registry.timer("ftgo.orders.approval.duration", "domain", "order")).isNotNull();
    }
}
