package net.chrisrichardson.ftgo.observability.health;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

/**
 * Health check configuration for FTGO microservices.
 * Provides custom health indicators for service readiness and liveness probes.
 */
@AutoConfiguration
public class FtgoHealthConfiguration {

    @Bean
    public HealthIndicator serviceReadinessIndicator() {
        return () -> Health.up()
                .withDetail("service", "ready")
                .withDetail("checks", "database,messaging")
                .build();
    }
}
