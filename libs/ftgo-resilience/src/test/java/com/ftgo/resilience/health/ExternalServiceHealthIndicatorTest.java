package com.ftgo.resilience.health;

import com.ftgo.resilience.config.ResilienceProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;

import static org.assertj.core.api.Assertions.assertThat;

class ExternalServiceHealthIndicatorTest {

    private ExternalServiceHealthIndicator healthIndicator;

    @BeforeEach
    void setUp() {
        ResilienceProperties properties = new ResilienceProperties();
        healthIndicator = new ExternalServiceHealthIndicator(properties);
    }

    @Test
    void shouldReturnUpWhenNoServicesRegistered() {
        Health health = healthIndicator.health();
        assertThat(health.getStatus()).isEqualTo(Status.UP);
    }

    @Test
    void shouldReturnUpWhenAllServicesHealthy() {
        healthIndicator.markServiceHealthy("payment-service");
        healthIndicator.markServiceHealthy("notification-service");

        Health health = healthIndicator.health();
        assertThat(health.getStatus()).isEqualTo(Status.UP);
        assertThat(health.getDetails()).containsEntry("totalServices", 2);
        assertThat(health.getDetails()).containsEntry("unhealthyServices", 0L);
    }

    @Test
    void shouldReturnDownWhenAnyServiceUnhealthy() {
        healthIndicator.markServiceHealthy("payment-service");
        healthIndicator.markServiceUnhealthy("notification-service", "Connection timeout");

        Health health = healthIndicator.health();
        assertThat(health.getStatus()).isEqualTo(Status.DOWN);
        assertThat(health.getDetails()).containsEntry("unhealthyServices", 1L);
    }

    @Test
    void shouldTrackServiceRecovery() {
        healthIndicator.markServiceUnhealthy("payment-service", "Error");
        assertThat(healthIndicator.isServiceHealthy("payment-service")).isFalse();

        healthIndicator.markServiceHealthy("payment-service");
        assertThat(healthIndicator.isServiceHealthy("payment-service")).isTrue();
    }

    @Test
    void shouldRegisterNewService() {
        healthIndicator.registerService("new-service");
        assertThat(healthIndicator.isServiceHealthy("new-service")).isTrue();
    }
}
