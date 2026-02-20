package com.ftgo.resilience.discovery;

import com.ftgo.resilience.config.ResilienceProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;

import static org.assertj.core.api.Assertions.assertThat;

class ServiceDiscoveryHealthIndicatorTest {

    private ServiceRegistry serviceRegistry;
    private ServiceDiscoveryHealthIndicator healthIndicator;

    @BeforeEach
    void setUp() {
        ResilienceProperties properties = new ResilienceProperties();
        serviceRegistry = new ServiceRegistry(properties);
        healthIndicator = new ServiceDiscoveryHealthIndicator(serviceRegistry);
    }

    @Test
    void shouldReturnUpWhenAllServicesHealthy() {
        Health health = healthIndicator.health();
        assertThat(health.getStatus()).isEqualTo(Status.UP);
        assertThat(health.getDetails()).containsEntry("discoveryType", "kubernetes");
        assertThat(health.getDetails()).containsEntry("totalServices", 4);
    }

    @Test
    void shouldReturnDownWhenServiceUnhealthy() {
        serviceRegistry.getService("order-service")
                .ifPresent(s -> s.setHealthy(false));

        Health health = healthIndicator.health();
        assertThat(health.getStatus()).isEqualTo(Status.DOWN);
        assertThat(health.getDetails()).containsEntry("unhealthyServices", 1L);
    }

    @Test
    void shouldIncludeServiceDetails() {
        Health health = healthIndicator.health();
        assertThat(health.getDetails()).containsKey("service.order-service.host");
        assertThat(health.getDetails()).containsKey("service.order-service.port");
        assertThat(health.getDetails()).containsKey("service.order-service.healthy");
    }
}
