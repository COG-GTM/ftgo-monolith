package com.ftgo.resilience.health;

import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;

import static org.assertj.core.api.Assertions.assertThat;

class CircuitBreakerHealthIndicatorTest {

    @Test
    void shouldReturnUpWhenNoRegistryConfigured() {
        CircuitBreakerHealthIndicator indicator = new CircuitBreakerHealthIndicator();
        Health health = indicator.health();
        assertThat(health.getStatus()).isEqualTo(Status.UP);
        assertThat(health.getDetails()).containsKey("circuitBreakers");
    }
}
