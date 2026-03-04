package com.ftgo.gateway.config;

import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import io.github.resilience4j.timelimiter.TimeLimiterRegistry;
import org.springframework.cloud.circuitbreaker.resilience4j.ReactiveResilience4JCircuitBreakerFactory;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JConfigBuilder;
import org.springframework.cloud.client.circuitbreaker.Customizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * Circuit breaker configuration for the API Gateway using Resilience4j.
 *
 * <p>Configures default and per-service circuit breaker settings:
 * <ul>
 *   <li>Sliding window of 10 requests</li>
 *   <li>Failure rate threshold of 50%</li>
 *   <li>Wait 10 seconds in open state before half-open</li>
 *   <li>3 permitted calls in half-open state</li>
 *   <li>Time limiter of 4 seconds per request</li>
 * </ul>
 */
@Configuration
public class CircuitBreakerConfig {

    @Bean
    public Customizer<ReactiveResilience4JCircuitBreakerFactory> defaultCustomizer() {
        return factory -> factory.configureDefault(id -> new Resilience4JConfigBuilder(id)
                .circuitBreakerConfig(io.github.resilience4j.circuitbreaker.CircuitBreakerConfig.custom()
                        .slidingWindowType(io.github.resilience4j.circuitbreaker.CircuitBreakerConfig.SlidingWindowType.COUNT_BASED)
                        .slidingWindowSize(10)
                        .failureRateThreshold(50)
                        .waitDurationInOpenState(Duration.ofSeconds(10))
                        .permittedNumberOfCallsInHalfOpenState(3)
                        .minimumNumberOfCalls(5)
                        .build())
                .timeLimiterConfig(TimeLimiterConfig.custom()
                        .timeoutDuration(Duration.ofSeconds(4))
                        .build())
                .build());
    }

    /**
     * Custom circuit breaker for Order Service with higher thresholds.
     */
    @Bean
    public Customizer<ReactiveResilience4JCircuitBreakerFactory> orderServiceCustomizer() {
        return factory -> factory.configure(builder -> builder
                        .circuitBreakerConfig(io.github.resilience4j.circuitbreaker.CircuitBreakerConfig.custom()
                                .slidingWindowSize(20)
                                .failureRateThreshold(50)
                                .waitDurationInOpenState(Duration.ofSeconds(15))
                                .permittedNumberOfCallsInHalfOpenState(5)
                                .minimumNumberOfCalls(10)
                                .build())
                        .timeLimiterConfig(TimeLimiterConfig.custom()
                                .timeoutDuration(Duration.ofSeconds(5))
                                .build()),
                "orderServiceCircuitBreaker");
    }
}
