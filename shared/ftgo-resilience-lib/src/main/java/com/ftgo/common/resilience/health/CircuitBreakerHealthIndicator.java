package com.ftgo.common.resilience.health;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Health indicator that reports the state of all registered circuit breakers.
 *
 * <p>Provides detailed information about each circuit breaker including:</p>
 * <ul>
 *   <li>State (CLOSED, OPEN, HALF_OPEN, DISABLED, FORCED_OPEN)</li>
 *   <li>Failure rate</li>
 *   <li>Slow call rate</li>
 *   <li>Number of buffered calls</li>
 *   <li>Number of failed calls</li>
 * </ul>
 */
public class CircuitBreakerHealthIndicator implements HealthIndicator {

    @Autowired(required = false)
    private CircuitBreakerRegistry circuitBreakerRegistry;

    @Override
    public Health health() {
        if (circuitBreakerRegistry == null) {
            return Health.up()
                    .withDetail("circuitBreakers", "No circuit breaker registry available")
                    .build();
        }

        Health.Builder builder = Health.up();
        Map<String, Object> circuitBreakers = new LinkedHashMap<>();
        boolean anyOpen = false;

        for (CircuitBreaker cb : circuitBreakerRegistry.getAllCircuitBreakers()) {
            Map<String, Object> cbDetails = new LinkedHashMap<>();
            CircuitBreaker.State state = cb.getState();
            CircuitBreaker.Metrics metrics = cb.getMetrics();

            cbDetails.put("state", state.name());
            cbDetails.put("failureRate", metrics.getFailureRate() + "%");
            cbDetails.put("slowCallRate", metrics.getSlowCallRate() + "%");
            cbDetails.put("bufferedCalls", metrics.getNumberOfBufferedCalls());
            cbDetails.put("failedCalls", metrics.getNumberOfFailedCalls());
            cbDetails.put("successfulCalls", metrics.getNumberOfSuccessfulCalls());
            cbDetails.put("notPermittedCalls", metrics.getNumberOfNotPermittedCalls());

            circuitBreakers.put(cb.getName(), cbDetails);

            if (state == CircuitBreaker.State.OPEN) {
                anyOpen = true;
            }
        }

        builder.withDetail("circuitBreakers", circuitBreakers);

        if (anyOpen) {
            return builder.status("DEGRADED").build();
        }

        return builder.build();
    }
}
