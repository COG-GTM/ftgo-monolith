package net.chrisrichardson.ftgo.resilience;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import net.chrisrichardson.ftgo.resilience.health.DownstreamServiceHealthIndicator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link DownstreamServiceHealthIndicator}.
 */
class DownstreamServiceHealthIndicatorTest {

    private CircuitBreakerRegistry registry;
    private DownstreamServiceHealthIndicator indicator;

    @BeforeEach
    void setUp() {
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
                .failureRateThreshold(50)
                .slidingWindowSize(5)
                .minimumNumberOfCalls(5)
                .build();
        registry = CircuitBreakerRegistry.of(config);
        indicator = new DownstreamServiceHealthIndicator(registry);
    }

    @Test
    @DisplayName("Reports UP when all circuit breakers are closed")
    void reportsUpWhenAllClosed() {
        registry.circuitBreaker("order-service");
        registry.circuitBreaker("consumer-service");

        Health health = indicator.health();

        assertThat(health.getStatus()).isEqualTo(Status.UP);
    }

    @Test
    @DisplayName("Reports DOWN when a circuit breaker is open")
    void reportsDownWhenCircuitBreakerOpen() {
        CircuitBreaker cb = registry.circuitBreaker("order-service");

        // Force circuit breaker open by recording failures
        for (int i = 0; i < 5; i++) {
            cb.onError(0, TimeUnit.MILLISECONDS, new RuntimeException("failure"));
        }

        assertThat(cb.getState()).isEqualTo(CircuitBreaker.State.OPEN);

        Health health = indicator.health();
        assertThat(health.getStatus()).isEqualTo(Status.DOWN);
    }

    @Test
    @DisplayName("Includes circuit breaker details in health response")
    @SuppressWarnings("unchecked")
    void includesDetailsInHealthResponse() {
        registry.circuitBreaker("order-service");

        Health health = indicator.health();

        Map<String, Object> details = health.getDetails();
        assertThat(details).containsKey("order-service");

        Map<String, Object> cbDetails = (Map<String, Object>) details.get("order-service");
        assertThat(cbDetails).containsKey("state");
        assertThat(cbDetails).containsKey("failureRate");
        assertThat(cbDetails).containsKey("bufferedCalls");
        assertThat(cbDetails).containsKey("failedCalls");
        assertThat(cbDetails).containsKey("successfulCalls");
    }

    @Test
    @DisplayName("Reports UP with warning when circuit breaker is half-open")
    void reportsUpWithWarningWhenHalfOpen() {
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
                .failureRateThreshold(50)
                .slidingWindowSize(5)
                .minimumNumberOfCalls(5)
                .automaticTransitionFromOpenToHalfOpenEnabled(false)
                .build();
        CircuitBreakerRegistry localRegistry = CircuitBreakerRegistry.of(config);
        CircuitBreaker cb = localRegistry.circuitBreaker("order-service");

        // Force open then transition to half-open
        for (int i = 0; i < 5; i++) {
            cb.onError(0, TimeUnit.MILLISECONDS, new RuntimeException("failure"));
        }
        cb.transitionToHalfOpenState();

        DownstreamServiceHealthIndicator localIndicator =
                new DownstreamServiceHealthIndicator(localRegistry);
        Health health = localIndicator.health();

        assertThat(health.getStatus()).isEqualTo(Status.UP);
        assertThat(health.getDetails()).containsKey("warning");
    }
}
