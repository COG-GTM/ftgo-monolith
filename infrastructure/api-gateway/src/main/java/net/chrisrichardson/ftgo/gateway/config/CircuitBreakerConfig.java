package net.chrisrichardson.ftgo.gateway.config;

import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import io.github.resilience4j.timelimiter.TimeLimiterRegistry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.circuitbreaker.resilience4j.ReactiveResilience4JCircuitBreakerFactory;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JConfigBuilder;
import org.springframework.cloud.client.circuitbreaker.Customizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * Resilience4j circuit breaker configuration for gateway routes.
 *
 * <p>Each downstream service has its own circuit breaker instance with configurable:
 * <ul>
 *   <li>Failure rate threshold (percentage)</li>
 *   <li>Wait duration in open state before transitioning to half-open</li>
 *   <li>Sliding window size for failure rate calculation</li>
 *   <li>Permitted calls in half-open state</li>
 *   <li>Time limiter for request timeout</li>
 * </ul>
 */
@Configuration
public class CircuitBreakerConfig {

    @Value("${ftgo.gateway.circuit-breaker.failure-rate-threshold:50}")
    private float failureRateThreshold;

    @Value("${ftgo.gateway.circuit-breaker.wait-duration-in-open-state:30}")
    private long waitDurationInOpenState;

    @Value("${ftgo.gateway.circuit-breaker.sliding-window-size:10}")
    private int slidingWindowSize;

    @Value("${ftgo.gateway.circuit-breaker.permitted-calls-in-half-open:3}")
    private int permittedCallsInHalfOpen;

    @Value("${ftgo.gateway.circuit-breaker.timeout-duration:5}")
    private long timeoutDuration;

    @Bean
    public Customizer<ReactiveResilience4JCircuitBreakerFactory> defaultCustomizer() {
        return factory -> factory.configureDefault(id -> new Resilience4JConfigBuilder(id)
                .circuitBreakerConfig(io.github.resilience4j.circuitbreaker.CircuitBreakerConfig.custom()
                        .failureRateThreshold(failureRateThreshold)
                        .waitDurationInOpenState(Duration.ofSeconds(waitDurationInOpenState))
                        .slidingWindowSize(slidingWindowSize)
                        .permittedNumberOfCallsInHalfOpenState(permittedCallsInHalfOpen)
                        .build())
                .timeLimiterConfig(TimeLimiterConfig.custom()
                        .timeoutDuration(Duration.ofSeconds(timeoutDuration))
                        .build())
                .build());
    }
}
