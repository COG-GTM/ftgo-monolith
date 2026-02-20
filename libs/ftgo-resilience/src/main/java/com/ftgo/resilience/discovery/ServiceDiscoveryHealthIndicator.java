package com.ftgo.resilience.discovery;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;

import java.util.Map;

public class ServiceDiscoveryHealthIndicator implements HealthIndicator {

    private final ServiceRegistry serviceRegistry;

    public ServiceDiscoveryHealthIndicator(ServiceRegistry serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
    }

    @Override
    public Health health() {
        Map<String, ServiceRegistry.ServiceInstance> allServices = serviceRegistry.getAllServices();

        if (allServices.isEmpty()) {
            return Health.unknown()
                    .withDetail("reason", "No services registered")
                    .build();
        }

        long healthyCount = allServices.values().stream()
                .filter(ServiceRegistry.ServiceInstance::isHealthy)
                .count();

        long unhealthyCount = allServices.size() - healthyCount;

        Health.Builder builder = unhealthyCount == 0 ? Health.up() : Health.down();

        builder.withDetail("discoveryType", serviceRegistry.getDiscoveryType());
        builder.withDetail("totalServices", allServices.size());
        builder.withDetail("healthyServices", healthyCount);
        builder.withDetail("unhealthyServices", unhealthyCount);

        allServices.forEach((name, instance) -> {
            builder.withDetail("service." + name + ".host", instance.getHost());
            builder.withDetail("service." + name + ".port", instance.getPort());
            builder.withDetail("service." + name + ".healthy", instance.isHealthy());
        });

        return builder.build();
    }
}
