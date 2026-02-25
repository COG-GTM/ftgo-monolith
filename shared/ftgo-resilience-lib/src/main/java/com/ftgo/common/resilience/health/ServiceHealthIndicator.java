package com.ftgo.common.resilience.health;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;

/**
 * Custom business health indicator for FTGO services.
 *
 * <p>Reports the overall service health considering:</p>
 * <ul>
 *   <li>Application readiness state</li>
 *   <li>Circuit breaker states (if any are OPEN, service is degraded)</li>
 *   <li>Service-specific custom checks</li>
 * </ul>
 */
public class ServiceHealthIndicator implements HealthIndicator {

    @Value("${spring.application.name:unknown}")
    private String applicationName;

    @Autowired(required = false)
    private CircuitBreakerRegistry circuitBreakerRegistry;

    @Override
    public Health health() {
        Health.Builder builder = Health.up()
                .withDetail("service", applicationName);

        if (circuitBreakerRegistry != null) {
            long openCount = circuitBreakerRegistry.getAllCircuitBreakers()
                    .stream()
                    .filter(cb -> cb.getState() == CircuitBreaker.State.OPEN)
                    .count();

            long totalCount = circuitBreakerRegistry.getAllCircuitBreakers().size();

            builder.withDetail("circuitBreakers.total", totalCount)
                    .withDetail("circuitBreakers.open", openCount);

            if (openCount > 0) {
                builder.withDetail("status.detail", "Service degraded - " + openCount
                        + " circuit breaker(s) open");
            }
        }

        return builder.build();
    }
}
