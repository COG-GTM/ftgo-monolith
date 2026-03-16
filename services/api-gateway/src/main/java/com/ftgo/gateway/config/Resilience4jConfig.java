package com.ftgo.gateway.config;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * Resilience4j circuit breaker configuration for downstream services.
 *
 * <p>Registers a named {@link CircuitBreaker} for each microservice.
 * When a downstream service fails beyond the configured thresholds, the
 * circuit opens and requests are rejected quickly without waiting for
 * timeouts, protecting the gateway from cascading failures.
 */
@Configuration
public class Resilience4jConfig {

    @Bean
    public CircuitBreakerConfig defaultCircuitBreakerConfig() {
        return CircuitBreakerConfig.custom()
                .failureRateThreshold(50)
                .waitDurationInOpenState(Duration.ofSeconds(30))
                .ringBufferSizeInClosedState(10)
                .ringBufferSizeInHalfOpenState(5)
                .build();
    }

    @Bean
    public CircuitBreakerRegistry circuitBreakerRegistry(CircuitBreakerConfig defaultConfig) {
        CircuitBreakerRegistry registry = CircuitBreakerRegistry.of(defaultConfig);

        registry.circuitBreaker("order-service");
        registry.circuitBreaker("consumer-service");
        registry.circuitBreaker("restaurant-service");
        registry.circuitBreaker("courier-service");

        return registry;
    }
}
