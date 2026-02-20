package com.ftgo.resilience.health;

import com.ftgo.resilience.config.ResilienceProperties;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ExternalServiceHealthIndicator implements HealthIndicator {

    private final ResilienceProperties properties;
    private final Map<String, ServiceStatus> serviceStatuses = new ConcurrentHashMap<>();

    public ExternalServiceHealthIndicator(ResilienceProperties properties) {
        this.properties = properties;
    }

    @Override
    public Health health() {
        if (serviceStatuses.isEmpty()) {
            return Health.up()
                    .withDetail("services", "No external services registered")
                    .build();
        }

        boolean allHealthy = serviceStatuses.values().stream()
                .allMatch(ServiceStatus::isHealthy);

        long unhealthyCount = serviceStatuses.values().stream()
                .filter(s -> !s.isHealthy())
                .count();

        Health.Builder builder = allHealthy ? Health.up() : Health.down();
        builder.withDetail("totalServices", serviceStatuses.size());
        builder.withDetail("unhealthyServices", unhealthyCount);
        builder.withDetail("timeoutMs", properties.getHealthCheck().getTimeoutMs());

        serviceStatuses.forEach((name, status) -> {
            builder.withDetail("service." + name + ".status", status.isHealthy() ? "UP" : "DOWN");
            builder.withDetail("service." + name + ".lastChecked", status.getLastCheckedTimestamp());
            if (status.getLastError() != null) {
                builder.withDetail("service." + name + ".error", status.getLastError());
            }
        });

        return builder.build();
    }

    public void registerService(String serviceName) {
        serviceStatuses.putIfAbsent(serviceName, new ServiceStatus());
    }

    public void markServiceHealthy(String serviceName) {
        serviceStatuses.computeIfAbsent(serviceName, k -> new ServiceStatus()).markHealthy();
    }

    public void markServiceUnhealthy(String serviceName, String error) {
        serviceStatuses.computeIfAbsent(serviceName, k -> new ServiceStatus()).markUnhealthy(error);
    }

    public boolean isServiceHealthy(String serviceName) {
        ServiceStatus status = serviceStatuses.get(serviceName);
        return status != null && status.isHealthy();
    }

    public static class ServiceStatus {

        private volatile boolean healthy = true;
        private volatile String lastError;
        private volatile long lastCheckedTimestamp;

        public void markHealthy() {
            this.healthy = true;
            this.lastError = null;
            this.lastCheckedTimestamp = System.currentTimeMillis();
        }

        public void markUnhealthy(String error) {
            this.healthy = false;
            this.lastError = error;
            this.lastCheckedTimestamp = System.currentTimeMillis();
        }

        public boolean isHealthy() {
            return healthy;
        }

        public String getLastError() {
            return lastError;
        }

        public long getLastCheckedTimestamp() {
            return lastCheckedTimestamp;
        }
    }
}
