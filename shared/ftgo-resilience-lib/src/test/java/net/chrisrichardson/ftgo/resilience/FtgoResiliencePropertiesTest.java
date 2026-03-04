package net.chrisrichardson.ftgo.resilience;

import net.chrisrichardson.ftgo.resilience.config.FtgoResilienceProperties;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link FtgoResilienceProperties} default values.
 */
class FtgoResiliencePropertiesTest {

    @Test
    @DisplayName("Default properties have expected values")
    void defaultPropertiesHaveExpectedValues() {
        FtgoResilienceProperties props = new FtgoResilienceProperties();

        assertThat(props.isEnabled()).isTrue();

        // Circuit breaker defaults
        assertThat(props.getCircuitBreaker().getFailureRateThreshold()).isEqualTo(50f);
        assertThat(props.getCircuitBreaker().getSlidingWindowSize()).isEqualTo(10);
        assertThat(props.getCircuitBreaker().getMinimumNumberOfCalls()).isEqualTo(5);
        assertThat(props.getCircuitBreaker().getWaitDurationInOpenState()).isEqualTo(Duration.ofSeconds(30));
        assertThat(props.getCircuitBreaker().getPermittedNumberOfCallsInHalfOpenState()).isEqualTo(3);
        assertThat(props.getCircuitBreaker().isAutomaticTransitionFromOpenToHalfOpenEnabled()).isTrue();

        // Retry defaults
        assertThat(props.getRetry().getMaxAttempts()).isEqualTo(3);
        assertThat(props.getRetry().getWaitDuration()).isEqualTo(Duration.ofSeconds(1));
        assertThat(props.getRetry().getMultiplier()).isEqualTo(2.0);
        assertThat(props.getRetry().isExponentialBackoff()).isTrue();

        // Bulkhead defaults
        assertThat(props.getBulkhead().getMaxConcurrentCalls()).isEqualTo(25);
        assertThat(props.getBulkhead().getMaxWaitDuration()).isEqualTo(Duration.ZERO);

        // Rate limiter defaults
        assertThat(props.getRateLimiter().getLimitForPeriod()).isEqualTo(50);
        assertThat(props.getRateLimiter().getLimitRefreshPeriod()).isEqualTo(Duration.ofSeconds(1));
        assertThat(props.getRateLimiter().getTimeoutDuration()).isEqualTo(Duration.ofMillis(500));

        // Graceful shutdown defaults
        assertThat(props.getGracefulShutdown().getTimeout()).isEqualTo(Duration.ofSeconds(30));
        assertThat(props.getGracefulShutdown().isAwaitTermination()).isTrue();
    }

    @Test
    @DisplayName("Per-service overrides can be configured")
    void perServiceOverridesCanBeConfigured() {
        FtgoResilienceProperties props = new FtgoResilienceProperties();

        FtgoResilienceProperties.ServiceResilienceProperties serviceProps =
                new FtgoResilienceProperties.ServiceResilienceProperties();
        FtgoResilienceProperties.CircuitBreakerProperties cbProps =
                new FtgoResilienceProperties.CircuitBreakerProperties();
        cbProps.setFailureRateThreshold(80);
        serviceProps.setCircuitBreaker(cbProps);

        props.getServices().put("order-service", serviceProps);

        assertThat(props.getServices()).containsKey("order-service");
        assertThat(props.getServices().get("order-service").getCircuitBreaker()
                .getFailureRateThreshold()).isEqualTo(80f);
    }
}
