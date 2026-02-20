package com.ftgo.observability.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ConsumerMetricsTest {

    private MeterRegistry registry;
    private ConsumerMetrics consumerMetrics;

    @BeforeEach
    void setUp() {
        registry = new SimpleMeterRegistry();
        consumerMetrics = new ConsumerMetrics(registry);
        consumerMetrics.init();
    }

    @Test
    void shouldIncrementConsumersRegisteredCounter() {
        consumerMetrics.incrementConsumersRegistered();
        consumerMetrics.incrementConsumersRegistered();

        assertThat(registry.counter("ftgo.consumers.registered", "domain", "consumer").count()).isEqualTo(2.0);
    }

    @Test
    void shouldIncrementConsumerVerificationsCounter() {
        consumerMetrics.incrementConsumerVerifications();

        assertThat(registry.counter("ftgo.consumers.verifications", "domain", "consumer").count()).isEqualTo(1.0);
    }

    @Test
    void shouldTrackActiveConsumersGauge() {
        consumerMetrics.setActiveConsumers(42);

        assertThat(registry.get("ftgo.consumers.active").gauge().value()).isEqualTo(42.0);
    }
}
