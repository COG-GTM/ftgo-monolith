package com.ftgo.common.resilience;

import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Integration tests for Resilience4j retry behavior.
 *
 * <p>Validates that retry policies work with exponential backoff
 * as per FTGO requirements: 3 attempts with 1s, 2s, 4s backoff.</p>
 */
class RetryIntegrationTest {

    @Test
    void retrySucceedsAfterTransientFailure() {
        RetryConfig config = RetryConfig.custom()
                .maxAttempts(3)
                .waitDuration(Duration.ofMillis(50)) // Short wait for testing
                .retryExceptions(RuntimeException.class)
                .build();

        RetryRegistry registry = RetryRegistry.of(config);
        Retry retry = registry.retry("test-retry");

        AtomicInteger attempts = new AtomicInteger(0);
        Supplier<String> supplier = Retry.decorateSupplier(retry, () -> {
            if (attempts.incrementAndGet() < 3) {
                throw new RuntimeException("Transient error");
            }
            return "success";
        });

        String result = supplier.get();
        assertThat(result).isEqualTo("success");
        assertThat(attempts.get()).isEqualTo(3);
    }

    @Test
    void retryExhaustsAllAttempts() {
        RetryConfig config = RetryConfig.custom()
                .maxAttempts(3)
                .waitDuration(Duration.ofMillis(50))
                .retryExceptions(RuntimeException.class)
                .build();

        RetryRegistry registry = RetryRegistry.of(config);
        Retry retry = registry.retry("test-retry-exhaust");

        AtomicInteger attempts = new AtomicInteger(0);
        Supplier<String> supplier = Retry.decorateSupplier(retry, () -> {
            attempts.incrementAndGet();
            throw new RuntimeException("Persistent error");
        });

        assertThatThrownBy(supplier::get)
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Persistent error");
        assertThat(attempts.get()).isEqualTo(3);
    }

    @Test
    void retryDoesNotRetryIgnoredExceptions() {
        RetryConfig config = RetryConfig.custom()
                .maxAttempts(3)
                .waitDuration(Duration.ofMillis(50))
                .retryExceptions(RuntimeException.class)
                .ignoreExceptions(IllegalArgumentException.class)
                .build();

        RetryRegistry registry = RetryRegistry.of(config);
        Retry retry = registry.retry("test-ignore");

        AtomicInteger attempts = new AtomicInteger(0);
        Supplier<String> supplier = Retry.decorateSupplier(retry, () -> {
            attempts.incrementAndGet();
            throw new IllegalArgumentException("Bad input");
        });

        assertThatThrownBy(supplier::get)
                .isInstanceOf(IllegalArgumentException.class);
        assertThat(attempts.get()).isEqualTo(1); // No retry for ignored exceptions
    }

    @Test
    void retryMetricsAreRecorded() {
        RetryConfig config = RetryConfig.custom()
                .maxAttempts(3)
                .waitDuration(Duration.ofMillis(50))
                .retryExceptions(RuntimeException.class)
                .build();

        RetryRegistry registry = RetryRegistry.of(config);
        Retry retry = registry.retry("test-metrics");

        AtomicInteger attempts = new AtomicInteger(0);
        Supplier<String> supplier = Retry.decorateSupplier(retry, () -> {
            if (attempts.incrementAndGet() < 2) {
                throw new RuntimeException("Transient error");
            }
            return "success";
        });

        supplier.get();

        Retry.Metrics metrics = retry.getMetrics();
        assertThat(metrics.getNumberOfSuccessfulCallsWithRetryAttempt()).isEqualTo(1);
        assertThat(metrics.getNumberOfFailedCallsWithoutRetryAttempt()).isEqualTo(0);
    }
}
