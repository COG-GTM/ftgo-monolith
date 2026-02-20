package com.ftgo.resilience.health;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;

public class CircuitBreakerHealthIndicator implements HealthIndicator {

    @Autowired(required = false)
    private CircuitBreakerRegistry circuitBreakerRegistry;

    @Override
    public Health health() {
        if (circuitBreakerRegistry == null) {
            return Health.up()
                    .withDetail("circuitBreakers", "No circuit breaker registry configured")
                    .build();
        }

        boolean anyOpen = false;
        Health.Builder builder = Health.up();

        for (CircuitBreaker cb : circuitBreakerRegistry.getAllCircuitBreakers()) {
            CircuitBreaker.State state = cb.getState();
            String name = cb.getName();

            builder.withDetail("circuitBreaker." + name + ".state", state.name());
            builder.withDetail("circuitBreaker." + name + ".failureRate",
                    cb.getMetrics().getFailureRate());
            builder.withDetail("circuitBreaker." + name + ".slowCallRate",
                    cb.getMetrics().getSlowCallRate());
            builder.withDetail("circuitBreaker." + name + ".bufferedCalls",
                    cb.getMetrics().getNumberOfBufferedCalls());

            if (state == CircuitBreaker.State.OPEN) {
                anyOpen = true;
            }
        }

        if (anyOpen) {
            builder = Health.down();
            for (CircuitBreaker cb : circuitBreakerRegistry.getAllCircuitBreakers()) {
                builder.withDetail("circuitBreaker." + cb.getName() + ".state",
                        cb.getState().name());
            }
            builder.withDetail("reason", "One or more circuit breakers are OPEN");
        }

        builder.withDetail("totalCircuitBreakers",
                circuitBreakerRegistry.getAllCircuitBreakers().size());

        return builder.build();
    }
}
