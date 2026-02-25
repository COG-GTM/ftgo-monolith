package com.ftgo.common.resilience;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Integration tests for Resilience4j circuit breaker behavior.
 *
 * <p>Validates that the circuit breaker opens after consecutive failures
 * and transitions to half-open state as expected.</p>
 */
class CircuitBreakerIntegrationTest {

    @Test
    void circuitBreakerOpensAfterConsecutiveFailures() {
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
                .failureRateThreshold(50)
                .minimumNumberOfCalls(5)
                .slidingWindowSize(10)
                .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.COUNT_BASED)
                .waitDurationInOpenState(Duration.ofSeconds(30))
                .permittedNumberOfCallsInHalfOpenState(3)
                .build();

        CircuitBreakerRegistry registry = CircuitBreakerRegistry.of(config);
        CircuitBreaker circuitBreaker = registry.circuitBreaker("test-service");

        assertThat(circuitBreaker.getState()).isEqualTo(CircuitBreaker.State.CLOSED);

        // Simulate 5 failures (meets minimum number of calls)
        Supplier<String> failingSupplier = CircuitBreaker.decorateSupplier(circuitBreaker, () -> {
            throw new RuntimeException("Service unavailable");
        });

        for (int i = 0; i < 5; i++) {
            try {
                failingSupplier.get();
            } catch (Exception ignored) {
                // Expected
            }
        }

        // Circuit breaker should be OPEN after 5 consecutive failures (100% failure rate > 50% threshold)
        assertThat(circuitBreaker.getState()).isEqualTo(CircuitBreaker.State.OPEN);

        // Verify metrics
        CircuitBreaker.Metrics metrics = circuitBreaker.getMetrics();
        assertThat(metrics.getNumberOfFailedCalls()).isEqualTo(5);
        assertThat(metrics.getFailureRate()).isEqualTo(100.0f);
    }

    @Test
    void circuitBreakerRemainsClosedOnSuccess() {
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
                .failureRateThreshold(50)
                .minimumNumberOfCalls(5)
                .slidingWindowSize(10)
                .build();

        CircuitBreakerRegistry registry = CircuitBreakerRegistry.of(config);
        CircuitBreaker circuitBreaker = registry.circuitBreaker("test-success");

        Supplier<String> successSupplier = CircuitBreaker.decorateSupplier(circuitBreaker,
                () -> "success");

        for (int i = 0; i < 10; i++) {
            successSupplier.get();
        }

        assertThat(circuitBreaker.getState()).isEqualTo(CircuitBreaker.State.CLOSED);
        assertThat(circuitBreaker.getMetrics().getNumberOfSuccessfulCalls()).isEqualTo(10);
    }

    @Test
    void circuitBreakerTransitionsToHalfOpen() {
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
                .failureRateThreshold(50)
                .minimumNumberOfCalls(5)
                .slidingWindowSize(10)
                .waitDurationInOpenState(Duration.ofMillis(100)) // Short wait for testing
                .permittedNumberOfCallsInHalfOpenState(3)
                .automaticTransitionFromOpenToHalfOpenEnabled(true)
                .build();

        CircuitBreakerRegistry registry = CircuitBreakerRegistry.of(config);
        CircuitBreaker circuitBreaker = registry.circuitBreaker("test-half-open");

        // Force circuit breaker to OPEN
        Supplier<String> failingSupplier = CircuitBreaker.decorateSupplier(circuitBreaker, () -> {
            throw new RuntimeException("Service unavailable");
        });

        for (int i = 0; i < 5; i++) {
            try {
                failingSupplier.get();
            } catch (Exception ignored) {
                // Expected
            }
        }

        assertThat(circuitBreaker.getState()).isEqualTo(CircuitBreaker.State.OPEN);

        // Wait for automatic transition to HALF_OPEN
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        assertThat(circuitBreaker.getState()).isEqualTo(CircuitBreaker.State.HALF_OPEN);
    }

    @Test
    void namedCircuitBreakersAreIndependent() {
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
                .failureRateThreshold(50)
                .minimumNumberOfCalls(5)
                .slidingWindowSize(10)
                .build();

        CircuitBreakerRegistry registry = CircuitBreakerRegistry.of(config);
        CircuitBreaker consumerCB = registry.circuitBreaker("consumer-service");
        CircuitBreaker restaurantCB = registry.circuitBreaker("restaurant-service");

        // Fail consumer service circuit breaker
        Supplier<String> failingConsumer = CircuitBreaker.decorateSupplier(consumerCB, () -> {
            throw new RuntimeException("Consumer service down");
        });

        for (int i = 0; i < 5; i++) {
            try {
                failingConsumer.get();
            } catch (Exception ignored) {
                // Expected
            }
        }

        // Consumer circuit breaker is OPEN, restaurant remains CLOSED
        assertThat(consumerCB.getState()).isEqualTo(CircuitBreaker.State.OPEN);
        assertThat(restaurantCB.getState()).isEqualTo(CircuitBreaker.State.CLOSED);
    }
}
