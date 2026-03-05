package net.chrisrichardson.ftgo.resilience.discovery;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Resolves service URLs using K8s DNS-based service discovery.
 *
 * <p>Usage:
 * <pre>
 * &#64;Autowired
 * private ServiceResolver serviceResolver;
 *
 * String orderServiceUrl = serviceResolver.resolve("order-service");
 * // Returns: http://ftgo-order-service.ftgo-dev.svc.cluster.local:8081
 * </pre>
 */
public class ServiceResolver {

    private static final Logger log = LoggerFactory.getLogger(ServiceResolver.class);

    private final KubernetesServiceDiscoveryProperties properties;

    public ServiceResolver(KubernetesServiceDiscoveryProperties properties) {
        this.properties = properties;
    }

    /**
     * Resolves the base URL for the given service name.
     *
     * @param serviceName the logical service name (e.g., "order-service")
     * @return the fully qualified base URL
     */
    public String resolve(String serviceName) {
        String url = properties.resolveServiceUrl(serviceName);
        log.debug("Resolved service '{}' to URL: {}", serviceName, url);
        return url;
    }

    /**
     * Resolves the full URL for the given service name and path.
     *
     * @param serviceName the logical service name
     * @param path        the API path (e.g., "/api/orders")
     * @return the fully qualified URL with path
     */
    public String resolve(String serviceName, String path) {
        String baseUrl = resolve(serviceName);
        String normalizedPath = path.startsWith("/") ? path : "/" + path;
        return baseUrl + normalizedPath;
    }

    /**
     * Returns the health check URL for the given service.
     *
     * @param serviceName the logical service name
     * @return the health endpoint URL
     */
    public String resolveHealthUrl(String serviceName) {
        return resolve(serviceName, "/actuator/health");
    }

    /**
     * Returns the configured namespace.
     */
    public String getNamespace() {
        return properties.getNamespace();
    }

    /**
     * Returns the configured cluster domain.
     */
    public String getClusterDomain() {
        return properties.getClusterDomain();
    }
}
