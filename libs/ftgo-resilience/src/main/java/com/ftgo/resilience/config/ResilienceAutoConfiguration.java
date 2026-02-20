package com.ftgo.resilience.config;

import com.ftgo.resilience.circuitbreaker.CircuitBreakerConfiguration;
import com.ftgo.resilience.retry.RetryConfiguration;
import com.ftgo.resilience.bulkhead.BulkheadConfiguration;
import com.ftgo.resilience.health.HealthCheckConfiguration;
import com.ftgo.resilience.discovery.ServiceDiscoveryConfiguration;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;

@AutoConfiguration
@ConditionalOnProperty(prefix = "ftgo.resilience", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(ResilienceProperties.class)
@Import({
        CircuitBreakerConfiguration.class,
        RetryConfiguration.class,
        BulkheadConfiguration.class,
        HealthCheckConfiguration.class,
        ServiceDiscoveryConfiguration.class
})
public class ResilienceAutoConfiguration {
}
