package com.ftgo.common.resilience.discovery;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry for Kubernetes service discovery using DNS-based resolution.
 *
 * <p>Maintains a registry of known FTGO services and their K8s DNS names.
 * Services are addressable via the standard Kubernetes DNS pattern:</p>
 * <pre>
 * {service-name}.{namespace}.svc.cluster.local:{port}
 * </pre>
 *
 * <p>Services can register custom port mappings and override URLs for
 * local development or testing scenarios.</p>
 */
public class KubernetesServiceRegistry {

    private static final Logger log = LoggerFactory.getLogger(KubernetesServiceRegistry.class);

    /** Default FTGO service ports. */
    private static final Map<String, Integer> DEFAULT_PORTS = Map.of(
            "order-service", 8081,
            "consumer-service", 8082,
            "restaurant-service", 8083,
            "courier-service", 8084,
            "api-gateway", 8080
    );

    private final String namespace;
    private final String clusterDomain;
    private final Map<String, ServiceRegistration> services = new ConcurrentHashMap<>();
    private final Map<String, String> overrideUrls = new ConcurrentHashMap<>();

    public KubernetesServiceRegistry(String namespace, String clusterDomain) {
        this.namespace = namespace;
        this.clusterDomain = clusterDomain;
        registerDefaultServices();
    }

    private void registerDefaultServices() {
        DEFAULT_PORTS.forEach((name, port) -> {
            String fqdn = buildFqdn(name);
            services.put(name, new ServiceRegistration(name, fqdn, port));
            log.debug("Registered K8s service: {} -> {}:{}", name, fqdn, port);
        });
    }

    /**
     * Registers or updates a service with a custom port.
     *
     * @param serviceName the service name (e.g., "order-service")
     * @param port the service port
     */
    public void registerService(String serviceName, int port) {
        String fqdn = buildFqdn(serviceName);
        services.put(serviceName, new ServiceRegistration(serviceName, fqdn, port));
        log.info("Registered K8s service: {} -> {}:{}", serviceName, fqdn, port);
    }

    /**
     * Sets an override URL for a service. Useful for local development
     * where K8s DNS is not available.
     *
     * @param serviceName the service name
     * @param url the override URL (e.g., "http://localhost:8081")
     */
    public void setOverrideUrl(String serviceName, String url) {
        overrideUrls.put(serviceName, url);
        log.info("Override URL set for service {}: {}", serviceName, url);
    }

    /**
     * Resolves the base URL for a service.
     *
     * <p>Returns the override URL if set, otherwise constructs the URL
     * from the K8s DNS name and port.</p>
     *
     * @param serviceName the service name
     * @return the base URL for the service
     * @throws IllegalArgumentException if the service is not registered
     */
    public String resolveBaseUrl(String serviceName) {
        // Check for override first (local dev / testing)
        String override = overrideUrls.get(serviceName);
        if (override != null) {
            return override;
        }

        ServiceRegistration registration = services.get(serviceName);
        if (registration == null) {
            throw new IllegalArgumentException("Unknown service: " + serviceName
                    + ". Register it first or set an override URL.");
        }

        return "http://" + registration.fqdn() + ":" + registration.port();
    }

    /**
     * Returns the K8s FQDN for a service.
     *
     * @param serviceName the short service name
     * @return fully qualified domain name
     */
    public String getFqdn(String serviceName) {
        return buildFqdn(serviceName);
    }

    /**
     * Checks if a service is registered.
     *
     * @param serviceName the service name
     * @return true if registered
     */
    public boolean isRegistered(String serviceName) {
        return services.containsKey(serviceName) || overrideUrls.containsKey(serviceName);
    }

    /**
     * Returns all registered services.
     *
     * @return unmodifiable map of service registrations
     */
    public Map<String, ServiceRegistration> getAllServices() {
        return Map.copyOf(services);
    }

    /**
     * Returns the namespace.
     */
    public String getNamespace() {
        return namespace;
    }

    /**
     * Returns the cluster domain.
     */
    public String getClusterDomain() {
        return clusterDomain;
    }

    private String buildFqdn(String serviceName) {
        return String.format("ftgo-%s.%s.%s", serviceName, namespace, clusterDomain);
    }

    /**
     * Represents a registered Kubernetes service.
     */
    public record ServiceRegistration(String name, String fqdn, int port) {
    }
}
