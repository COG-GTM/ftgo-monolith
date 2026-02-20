package com.ftgo.resilience.health;

import com.ftgo.resilience.config.ResilienceProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnClass(name = "org.springframework.boot.actuate.health.HealthIndicator")
@ConditionalOnProperty(prefix = "ftgo.resilience.health-check", name = "enabled", havingValue = "true", matchIfMissing = true)
public class HealthCheckConfiguration {

    @Bean
    @ConditionalOnProperty(prefix = "ftgo.resilience.health-check", name = "database-enabled", havingValue = "true", matchIfMissing = true)
    public DatabaseHealthIndicator databaseHealthIndicator(ResilienceProperties properties) {
        return new DatabaseHealthIndicator(properties);
    }

    @Bean
    @ConditionalOnProperty(prefix = "ftgo.resilience.health-check", name = "messaging-enabled", havingValue = "true", matchIfMissing = true)
    public MessagingHealthIndicator messagingHealthIndicator(ResilienceProperties properties) {
        return new MessagingHealthIndicator(properties);
    }

    @Bean
    @ConditionalOnProperty(prefix = "ftgo.resilience.health-check", name = "external-services-enabled", havingValue = "true", matchIfMissing = true)
    public ExternalServiceHealthIndicator externalServiceHealthIndicator(ResilienceProperties properties) {
        return new ExternalServiceHealthIndicator(properties);
    }

    @Bean
    public CircuitBreakerHealthIndicator circuitBreakerHealthIndicator() {
        return new CircuitBreakerHealthIndicator();
    }
}
