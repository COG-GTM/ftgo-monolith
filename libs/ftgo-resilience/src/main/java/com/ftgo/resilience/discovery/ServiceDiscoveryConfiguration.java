package com.ftgo.resilience.discovery;

import com.ftgo.resilience.config.ResilienceProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(prefix = "ftgo.resilience.discovery", name = "enabled", havingValue = "true", matchIfMissing = true)
public class ServiceDiscoveryConfiguration {

    @Bean
    public ServiceRegistry serviceRegistry(ResilienceProperties properties) {
        return new ServiceRegistry(properties);
    }

    @Bean
    public ServiceDiscoveryHealthIndicator serviceDiscoveryHealthIndicator(ServiceRegistry serviceRegistry) {
        return new ServiceDiscoveryHealthIndicator(serviceRegistry);
    }
}
