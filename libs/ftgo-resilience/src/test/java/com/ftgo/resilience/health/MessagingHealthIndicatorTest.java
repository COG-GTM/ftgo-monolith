package com.ftgo.resilience.health;

import com.ftgo.resilience.config.ResilienceProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;

import static org.assertj.core.api.Assertions.assertThat;

class MessagingHealthIndicatorTest {

    private MessagingHealthIndicator healthIndicator;

    @BeforeEach
    void setUp() {
        ResilienceProperties properties = new ResilienceProperties();
        healthIndicator = new MessagingHealthIndicator(properties);
    }

    @Test
    void shouldReturnUpByDefault() {
        Health health = healthIndicator.health();
        assertThat(health.getStatus()).isEqualTo(Status.UP);
        assertThat(health.getDetails()).containsEntry("messaging", "available");
    }

    @Test
    void shouldReturnDownWhenMarkedUnavailable() {
        healthIndicator.markUnavailable("Broker connection lost");

        Health health = healthIndicator.health();
        assertThat(health.getStatus()).isEqualTo(Status.DOWN);
        assertThat(health.getDetails()).containsEntry("messaging", "unavailable");
        assertThat(health.getDetails()).containsEntry("error", "Broker connection lost");
    }

    @Test
    void shouldReturnUpAfterRecovery() {
        healthIndicator.markUnavailable("Broker connection lost");
        healthIndicator.markAvailable();

        Health health = healthIndicator.health();
        assertThat(health.getStatus()).isEqualTo(Status.UP);
        assertThat(health.getDetails()).containsEntry("messaging", "available");
    }

    @Test
    void shouldTrackAvailabilityState() {
        assertThat(healthIndicator.isMessagingAvailable()).isTrue();

        healthIndicator.markUnavailable("error");
        assertThat(healthIndicator.isMessagingAvailable()).isFalse();

        healthIndicator.markAvailable();
        assertThat(healthIndicator.isMessagingAvailable()).isTrue();
    }
}
