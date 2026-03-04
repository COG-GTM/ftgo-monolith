package net.chrisrichardson.ftgo.resilience;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import net.chrisrichardson.ftgo.resilience.circuitbreaker.FtgoCircuitBreakerConfiguration;
import net.chrisrichardson.ftgo.resilience.config.FtgoResilienceProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link FtgoCircuitBreakerConfiguration}.
 */
class FtgoCircuitBreakerConfigurationTest {

    private FtgoResilienceProperties properties;
    private FtgoCircuitBreakerConfiguration configuration;

    @BeforeEach
    void setUp() {
        properties = new FtgoResilienceProperties();
        configuration = new FtgoCircuitBreakerConfiguration();
    }

    @Test
    @DisplayName("Circuit breaker config uses default values")
    void circuitBreakerConfigUsesDefaults() {
        CircuitBreakerConfig config = configuration.ftgoCircuitBreakerConfig(properties);

        assertThat(config.getFailureRateThreshold()).isEqualTo(50f);
        assertThat(config.getSlidingWindowSize()).isEqualTo(10);
        assertThat(config.getMinimumNumberOfCalls()).isEqualTo(5);
        assertThat(config.getWaitIntervalFunctionInOpenState().apply(1))
                .isEqualTo(30000L); // 30 seconds
        assertThat(config.getPermittedNumberOfCallsInHalfOpenState()).isEqualTo(3);
        assertThat(config.isAutomaticTransitionFromOpenToHalfOpenEnabled()).isTrue();
    }

    @Test
    @DisplayName("Circuit breaker config uses custom values")
    void circuitBreakerConfigUsesCustomValues() {
        FtgoResilienceProperties.CircuitBreakerProperties cbProps = properties.getCircuitBreaker();
        cbProps.setFailureRateThreshold(80);
        cbProps.setSlidingWindowSize(20);
        cbProps.setMinimumNumberOfCalls(10);
        cbProps.setWaitDurationInOpenState(Duration.ofSeconds(60));

        CircuitBreakerConfig config = configuration.ftgoCircuitBreakerConfig(properties);

        assertThat(config.getFailureRateThreshold()).isEqualTo(80f);
        assertThat(config.getSlidingWindowSize()).isEqualTo(20);
        assertThat(config.getMinimumNumberOfCalls()).isEqualTo(10);
    }

    @Test
    @DisplayName("Circuit breaker registry pre-registers service breakers")
    void registryPreRegistersServiceBreakers() {
        CircuitBreakerConfig config = configuration.ftgoCircuitBreakerConfig(properties);
        CircuitBreakerRegistry registry = configuration.ftgoCircuitBreakerRegistry(config);

        assertThat(registry.circuitBreaker("order-service")).isNotNull();
        assertThat(registry.circuitBreaker("consumer-service")).isNotNull();
        assertThat(registry.circuitBreaker("restaurant-service")).isNotNull();
        assertThat(registry.circuitBreaker("courier-service")).isNotNull();
    }

    @Test
    @DisplayName("Circuit breaker opens after consecutive failures")
    void circuitBreakerOpensAfterFailures() {
        // Configure: 5 minimum calls, 50% failure rate → opens after 5 failures out of 10
        FtgoResilienceProperties.CircuitBreakerProperties cbProps = properties.getCircuitBreaker();
        cbProps.setMinimumNumberOfCalls(5);
        cbProps.setSlidingWindowSize(10);
        cbProps.setFailureRateThreshold(50);

        CircuitBreakerConfig config = configuration.ftgoCircuitBreakerConfig(properties);
        CircuitBreakerRegistry registry = configuration.ftgoCircuitBreakerRegistry(config);
        CircuitBreaker cb = registry.circuitBreaker("test-service");

        // Record 5 consecutive failures
        for (int i = 0; i < 5; i++) {
            cb.onError(0, java.util.concurrent.TimeUnit.MILLISECONDS,
                    new RuntimeException("test failure"));
        }

        assertThat(cb.getState()).isEqualTo(CircuitBreaker.State.OPEN);
    }

    @Test
    @DisplayName("Circuit breaker supports TIME_BASED sliding window")
    void circuitBreakerSupportsTimeBasedWindow() {
        properties.getCircuitBreaker().setSlidingWindowType("TIME_BASED");

        CircuitBreakerConfig config = configuration.ftgoCircuitBreakerConfig(properties);
        assertThat(config.getSlidingWindowType())
                .isEqualTo(CircuitBreakerConfig.SlidingWindowType.TIME_BASED);
    }
}
