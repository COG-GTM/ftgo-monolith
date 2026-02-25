package com.ftgo.common.resilience.circuitbreaker;

import com.ftgo.common.resilience.config.ResilienceProperties;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig.SlidingWindowType;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * Configuration for Resilience4j circuit breakers.
 *
 * <p>Provides a default {@link CircuitBreakerRegistry} with FTGO-standard
 * settings: opens after 5 consecutive failures, half-opens after 30 seconds.</p>
 *
 * <p>Services can create named circuit breakers from the registry:</p>
 * <pre>
 * CircuitBreaker cb = circuitBreakerRegistry.circuitBreaker("consumer-service");
 * </pre>
 *
 * <p>Or use the {@code @CircuitBreaker} annotation:</p>
 * <pre>
 * &#64;CircuitBreaker(name = "consumer-service", fallbackMethod = "fallback")
 * public Consumer validateConsumer(long consumerId) { ... }
 * </pre>
 */
@Configuration
@ConditionalOnClass(CircuitBreaker.class)
public class CircuitBreakerConfiguration {

    private static final Logger log = LoggerFactory.getLogger(CircuitBreakerConfiguration.class);

    /**
     * Creates a default {@link CircuitBreakerConfig} with FTGO-standard settings.
     *
     * <ul>
     *   <li>Failure rate threshold: configurable (default 50%)</li>
     *   <li>Minimum calls before evaluation: 5</li>
     *   <li>Wait duration in open state: 30 seconds</li>
     *   <li>Permitted calls in half-open state: 3</li>
     *   <li>Sliding window: count-based, size 10</li>
     *   <li>Slow call threshold: 2 seconds, 80% rate</li>
     * </ul>
     */
    @Bean
    @ConditionalOnMissingBean(CircuitBreakerConfig.class)
    public CircuitBreakerConfig defaultCircuitBreakerConfig(ResilienceProperties properties) {
        ResilienceProperties.CircuitBreakerProperties cbProps = properties.getCircuitBreaker();

        SlidingWindowType windowType = "TIME_BASED".equalsIgnoreCase(cbProps.getSlidingWindowType())
                ? SlidingWindowType.TIME_BASED
                : SlidingWindowType.COUNT_BASED;

        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
                .failureRateThreshold(cbProps.getFailureRateThreshold())
                .minimumNumberOfCalls(cbProps.getMinimumNumberOfCalls())
                .permittedNumberOfCallsInHalfOpenState(cbProps.getPermittedNumberOfCallsInHalfOpenState())
                .waitDurationInOpenState(Duration.ofSeconds(cbProps.getWaitDurationInOpenStateSeconds()))
                .slidingWindowSize(cbProps.getSlidingWindowSize())
                .slidingWindowType(windowType)
                .slowCallDurationThreshold(Duration.ofSeconds(cbProps.getSlowCallDurationThresholdSeconds()))
                .slowCallRateThreshold(cbProps.getSlowCallRateThreshold())
                .automaticTransitionFromOpenToHalfOpenEnabled(true)
                .build();

        log.info("FTGO Circuit Breaker configured: failureRate={}%, minCalls={}, "
                        + "waitDuration={}s, slidingWindow={} ({})",
                cbProps.getFailureRateThreshold(),
                cbProps.getMinimumNumberOfCalls(),
                cbProps.getWaitDurationInOpenStateSeconds(),
                cbProps.getSlidingWindowSize(),
                windowType);

        return config;
    }

    /**
     * Creates a {@link CircuitBreakerRegistry} with the default configuration.
     *
     * <p>Named circuit breakers for specific services (e.g., "consumer-service",
     * "restaurant-service") are created from this registry.</p>
     */
    @Bean
    @ConditionalOnMissingBean(CircuitBreakerRegistry.class)
    public CircuitBreakerRegistry circuitBreakerRegistry(CircuitBreakerConfig defaultConfig) {
        return CircuitBreakerRegistry.of(defaultConfig);
    }
}
