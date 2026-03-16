package net.chrisrichardson.ftgo.resilience.circuitbreaker;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

/**
 * Factory for creating and managing Resilience4j circuit breaker instances.
 * Uses a shared registry so that circuit breakers are reused across the application.
 */
public class CircuitBreakerFactory {

    private static final Logger logger = LoggerFactory.getLogger(CircuitBreakerFactory.class);

    private final CircuitBreakerRegistry registry;

    public CircuitBreakerFactory(CircuitBreakerRegistry registry) {
        this.registry = registry;
    }

    /**
     * Returns a circuit breaker for the given service name, creating one if it does not exist.
     *
     * @param serviceName logical name of the downstream service
     * @return the circuit breaker instance
     */
    public CircuitBreaker getCircuitBreaker(String serviceName) {
        CircuitBreaker circuitBreaker = registry.circuitBreaker(serviceName);
        logger.debug("Retrieved circuit breaker for service: {}, state: {}",
                serviceName, circuitBreaker.getState());
        return circuitBreaker;
    }

    /**
     * Creates a circuit breaker with a custom configuration override.
     *
     * @param serviceName logical name of the downstream service
     * @param properties  custom configuration properties
     * @return the circuit breaker instance
     */
    public CircuitBreaker getCircuitBreaker(String serviceName,
                                            CircuitBreakerConfigurationProperties properties) {
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
                .failureRateThreshold(properties.getFailureRateThreshold())
                .ringBufferSizeInClosedState(properties.getRingBufferSizeInClosedState())
                .ringBufferSizeInHalfOpenState(properties.getRingBufferSizeInHalfOpenState())
                .waitDurationInOpenState(Duration.ofMillis(properties.getWaitDurationInOpenStateMillis()))
                .build();

        CircuitBreaker circuitBreaker = registry.circuitBreaker(serviceName, config);
        logger.info("Created circuit breaker for service: {} with custom config " +
                        "(failureRate={}%, ringBuffer={}, waitDuration={}ms)",
                serviceName,
                properties.getFailureRateThreshold(),
                properties.getRingBufferSizeInClosedState(),
                properties.getWaitDurationInOpenStateMillis());
        return circuitBreaker;
    }

    /**
     * Returns the underlying circuit breaker registry.
     */
    public CircuitBreakerRegistry getRegistry() {
        return registry;
    }
}
