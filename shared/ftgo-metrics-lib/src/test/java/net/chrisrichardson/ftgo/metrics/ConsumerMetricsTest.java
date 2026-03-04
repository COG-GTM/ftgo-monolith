package net.chrisrichardson.ftgo.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import net.chrisrichardson.ftgo.metrics.consumer.ConsumerMetrics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for {@link ConsumerMetrics}.
 */
class ConsumerMetricsTest {

    private MeterRegistry registry;
    private ConsumerMetrics consumerMetrics;

    @BeforeEach
    void setUp() {
        registry = new SimpleMeterRegistry();
        consumerMetrics = new ConsumerMetrics(registry);
    }

    @Test
    void shouldIncrementConsumersRegisteredCounter() {
        consumerMetrics.consumerRegistered();
        consumerMetrics.consumerRegistered();
        consumerMetrics.consumerRegistered();

        double count = registry.counter("consumers.registered", "service", "consumer-service").count();
        assertEquals(3.0, count);
    }

    @Test
    void shouldIncrementConsumersValidatedCounter() {
        consumerMetrics.consumerValidated();

        double count = registry.counter("consumers.validated", "service", "consumer-service").count();
        assertEquals(1.0, count);
    }
}
