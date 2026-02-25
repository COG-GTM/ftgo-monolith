package com.ftgo.common.resilience;

import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.bulkhead.BulkheadConfig;
import io.github.resilience4j.bulkhead.BulkheadFullException;
import io.github.resilience4j.bulkhead.BulkheadRegistry;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Integration tests for Resilience4j bulkhead behavior.
 *
 * <p>Validates that bulkhead limits concurrent calls to downstream services.</p>
 */
class BulkheadIntegrationTest {

    @Test
    void bulkheadLimitsConcurrentCalls() {
        BulkheadConfig config = BulkheadConfig.custom()
                .maxConcurrentCalls(2)
                .maxWaitDuration(Duration.ofMillis(0)) // No waiting
                .build();

        BulkheadRegistry registry = BulkheadRegistry.of(config);
        Bulkhead bulkhead = registry.bulkhead("test-bulkhead");

        // Acquire 2 permits
        bulkhead.acquirePermission();
        bulkhead.acquirePermission();

        // Third call should be rejected
        assertThatThrownBy(bulkhead::acquirePermission)
                .isInstanceOf(BulkheadFullException.class);

        // Release one permit, next call should succeed
        bulkhead.releasePermission();
        bulkhead.acquirePermission(); // Should not throw
    }

    @Test
    void bulkheadMetricsReportCorrectValues() {
        BulkheadConfig config = BulkheadConfig.custom()
                .maxConcurrentCalls(5)
                .maxWaitDuration(Duration.ofMillis(100))
                .build();

        BulkheadRegistry registry = BulkheadRegistry.of(config);
        Bulkhead bulkhead = registry.bulkhead("test-metrics");

        Bulkhead.Metrics metrics = bulkhead.getMetrics();
        assertThat(metrics.getAvailableConcurrentCalls()).isEqualTo(5);
        assertThat(metrics.getMaxAllowedConcurrentCalls()).isEqualTo(5);

        bulkhead.acquirePermission();
        assertThat(metrics.getAvailableConcurrentCalls()).isEqualTo(4);

        bulkhead.releasePermission();
        assertThat(metrics.getAvailableConcurrentCalls()).isEqualTo(5);
    }

    @Test
    void namedBulkheadsAreIndependent() {
        BulkheadConfig config = BulkheadConfig.custom()
                .maxConcurrentCalls(2)
                .maxWaitDuration(Duration.ofMillis(0))
                .build();

        BulkheadRegistry registry = BulkheadRegistry.of(config);
        Bulkhead consumerBulkhead = registry.bulkhead("consumer-service");
        Bulkhead restaurantBulkhead = registry.bulkhead("restaurant-service");

        // Fill consumer bulkhead
        consumerBulkhead.acquirePermission();
        consumerBulkhead.acquirePermission();

        // Restaurant bulkhead should still be available
        restaurantBulkhead.acquirePermission(); // Should not throw

        Bulkhead.Metrics consumerMetrics = consumerBulkhead.getMetrics();
        Bulkhead.Metrics restaurantMetrics = restaurantBulkhead.getMetrics();

        assertThat(consumerMetrics.getAvailableConcurrentCalls()).isEqualTo(0);
        assertThat(restaurantMetrics.getAvailableConcurrentCalls()).isEqualTo(1);
    }
}
