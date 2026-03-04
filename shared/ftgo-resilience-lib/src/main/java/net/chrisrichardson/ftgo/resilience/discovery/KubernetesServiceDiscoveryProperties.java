package net.chrisrichardson.ftgo.resilience.discovery;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.Map;

/**
 * Configuration properties for K8s-native DNS-based service discovery.
 *
 * <p>In Kubernetes, services are discoverable via DNS using the pattern:
 * {@code <service-name>.<namespace>.svc.cluster.local}
 *
 * <p>Example configuration:
 * <pre>
 * ftgo.discovery.namespace=ftgo-dev
 * ftgo.discovery.cluster-domain=cluster.local
 * ftgo.discovery.services.order-service.host=ftgo-order-service
 * ftgo.discovery.services.order-service.port=8081
 * ftgo.discovery.services.consumer-service.host=ftgo-consumer-service
 * ftgo.discovery.services.consumer-service.port=8082
 * </pre>
 *
 * <p>When running outside K8s (e.g., local development), override with direct URLs:
 * <pre>
 * ftgo.discovery.services.order-service.url=http://localhost:8081
 * </pre>
 */
@ConfigurationProperties(prefix = "ftgo.discovery")
public class KubernetesServiceDiscoveryProperties {

    /** K8s namespace where services are deployed. Default: default. */
    private String namespace = "default";

    /** K8s cluster domain. Default: cluster.local. */
    private String clusterDomain = "cluster.local";

    /** Per-service discovery configuration. */
    private Map<String, ServiceEndpoint> services = new HashMap<>();

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getClusterDomain() {
        return clusterDomain;
    }

    public void setClusterDomain(String clusterDomain) {
        this.clusterDomain = clusterDomain;
    }

    public Map<String, ServiceEndpoint> getServices() {
        return services;
    }

    public void setServices(Map<String, ServiceEndpoint> services) {
        this.services = services;
    }

    /**
     * Resolves the base URL for a given service using K8s DNS convention.
     *
     * @param serviceName the logical service name (e.g., "order-service")
     * @return the fully qualified URL
     */
    public String resolveServiceUrl(String serviceName) {
        ServiceEndpoint endpoint = services.get(serviceName);
        if (endpoint != null && endpoint.getUrl() != null && !endpoint.getUrl().isEmpty()) {
            return endpoint.getUrl();
        }
        if (endpoint != null) {
            String host = endpoint.getHost() != null ? endpoint.getHost() : "ftgo-" + serviceName;
            int port = endpoint.getPort() > 0 ? endpoint.getPort() : 8080;
            String scheme = endpoint.getScheme() != null ? endpoint.getScheme() : "http";
            return String.format("%s://%s.%s.svc.%s:%d",
                    scheme, host, namespace, clusterDomain, port);
        }
        // Default: K8s DNS convention
        return String.format("http://ftgo-%s.%s.svc.%s:8080",
                serviceName, namespace, clusterDomain);
    }

    /**
     * Configuration for a single service endpoint.
     */
    public static class ServiceEndpoint {

        /** Direct URL override (bypasses DNS resolution). */
        private String url;

        /** K8s service hostname. Default: ftgo-{service-name}. */
        private String host;

        /** Service port. Default: 8080. */
        private int port = 8080;

        /** URL scheme (http or https). Default: http. */
        private String scheme = "http";

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }

        public int getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
        }

        public String getScheme() {
            return scheme;
        }

        public void setScheme(String scheme) {
            this.scheme = scheme;
        }
    }
}
