package com.ftgo.resilience.discovery;

import com.ftgo.resilience.config.ResilienceProperties;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ServiceRegistry {

    private final ResilienceProperties properties;
    private final Map<String, ServiceInstance> services = new ConcurrentHashMap<>();

    public ServiceRegistry(ResilienceProperties properties) {
        this.properties = properties;
        registerDefaultServices();
    }

    private void registerDefaultServices() {
        register("order-service", "order-service", 8080);
        register("restaurant-service", "restaurant-service", 8080);
        register("consumer-service", "consumer-service", 8080);
        register("courier-service", "courier-service", 8080);
    }

    public void register(String serviceName, String host, int port) {
        services.put(serviceName, new ServiceInstance(serviceName, host, port));
    }

    public void deregister(String serviceName) {
        services.remove(serviceName);
    }

    public Optional<ServiceInstance> getService(String serviceName) {
        return Optional.ofNullable(services.get(serviceName));
    }

    public Set<String> getServiceNames() {
        return Collections.unmodifiableSet(services.keySet());
    }

    public Map<String, ServiceInstance> getAllServices() {
        return Collections.unmodifiableMap(services);
    }

    public String getDiscoveryType() {
        return properties.getDiscovery().getType();
    }

    public String resolveServiceUrl(String serviceName) {
        return getService(serviceName)
                .map(instance -> String.format("http://%s:%d", instance.getHost(), instance.getPort()))
                .orElseThrow(() -> new ServiceNotFoundException(
                        "Service not found: " + serviceName));
    }

    public static class ServiceInstance {

        private final String name;
        private final String host;
        private final int port;
        private volatile boolean healthy = true;

        public ServiceInstance(String name, String host, int port) {
            this.name = name;
            this.host = host;
            this.port = port;
        }

        public String getName() {
            return name;
        }

        public String getHost() {
            return host;
        }

        public int getPort() {
            return port;
        }

        public boolean isHealthy() {
            return healthy;
        }

        public void setHealthy(boolean healthy) {
            this.healthy = healthy;
        }
    }

    public static class ServiceNotFoundException extends RuntimeException {
        public ServiceNotFoundException(String message) {
            super(message);
        }
    }
}
