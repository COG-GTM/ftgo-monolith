package net.chrisrichardson.ftgo.resilience.circuitbreaker;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import net.chrisrichardson.ftgo.resilience.config.FtgoResilienceProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.TimeoutException;

/**
 * Configures Resilience4j Circuit Breaker for FTGO services.
 *
 * <p>Default behavior:
 * <ul>
 *   <li>Opens after 5 consecutive failures (sliding window of 10, threshold 50%)</li>
 *   <li>Half-opens after 30 seconds</li>
 *   <li>Permits 3 calls in half-open state to test recovery</li>
 *   <li>Automatically transitions from open to half-open</li>
 * </ul>
 *
 * <p>Records exceptions: {@link IOException}, {@link TimeoutException},
 * {@link RuntimeException}.
 */
@Configuration
@EnableConfigurationProperties(FtgoResilienceProperties.class)
public class FtgoCircuitBreakerConfiguration {

    private static final Logger log = LoggerFactory.getLogger(FtgoCircuitBreakerConfiguration.class);

    /**
     * Creates the default circuit breaker configuration from properties.
     */
    @Bean
    @ConditionalOnMissingBean
    public CircuitBreakerConfig ftgoCircuitBreakerConfig(FtgoResilienceProperties properties) {
        FtgoResilienceProperties.CircuitBreakerProperties cbProps = properties.getCircuitBreaker();

        CircuitBreakerConfig.SlidingWindowType windowType =
                "TIME_BASED".equalsIgnoreCase(cbProps.getSlidingWindowType())
                        ? CircuitBreakerConfig.SlidingWindowType.TIME_BASED
                        : CircuitBreakerConfig.SlidingWindowType.COUNT_BASED;

        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
                .failureRateThreshold(cbProps.getFailureRateThreshold())
                .slidingWindowSize(cbProps.getSlidingWindowSize())
                .slidingWindowType(windowType)
                .minimumNumberOfCalls(cbProps.getMinimumNumberOfCalls())
                .waitDurationInOpenState(cbProps.getWaitDurationInOpenState())
                .permittedNumberOfCallsInHalfOpenState(cbProps.getPermittedNumberOfCallsInHalfOpenState())
                .automaticTransitionFromOpenToHalfOpenEnabled(cbProps.isAutomaticTransitionFromOpenToHalfOpenEnabled())
                .recordExceptions(IOException.class, TimeoutException.class, RuntimeException.class)
                .build();

        log.info("FTGO Circuit Breaker config: failureRate={}%, window={} ({}), "
                        + "waitInOpen={}s, halfOpenCalls={}",
                cbProps.getFailureRateThreshold(),
                cbProps.getSlidingWindowSize(),
                windowType,
                cbProps.getWaitDurationInOpenState().getSeconds(),
                cbProps.getPermittedNumberOfCallsInHalfOpenState());

        return config;
    }

    /**
     * Creates the circuit breaker registry with the default configuration
     * and pre-registers circuit breakers for known FTGO services.
     */
    @Bean
    @ConditionalOnMissingBean
    public CircuitBreakerRegistry ftgoCircuitBreakerRegistry(CircuitBreakerConfig config) {
        CircuitBreakerRegistry registry = CircuitBreakerRegistry.of(config);

        // Pre-register circuit breakers for known downstream services
        String[] serviceNames = {
                "order-service", "consumer-service",
                "restaurant-service", "courier-service"
        };

        for (String serviceName : serviceNames) {
            CircuitBreaker cb = registry.circuitBreaker(serviceName);
            cb.getEventPublisher()
                    .onStateTransition(event ->
                            log.warn("Circuit breaker '{}' state transition: {}",
                                    event.getCircuitBreakerName(), event.getStateTransition()))
                    .onError(event ->
                            log.debug("Circuit breaker '{}' recorded error: {}",
                                    event.getCircuitBreakerName(),
                                    event.getThrowable().getMessage()));
        }

        log.info("FTGO Circuit Breaker registry initialized with {} pre-registered breakers",
                serviceNames.length);
        return registry;
    }
}
