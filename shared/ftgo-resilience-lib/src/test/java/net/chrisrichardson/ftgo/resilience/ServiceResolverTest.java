package net.chrisrichardson.ftgo.resilience;

import net.chrisrichardson.ftgo.resilience.discovery.KubernetesServiceDiscoveryProperties;
import net.chrisrichardson.ftgo.resilience.discovery.ServiceResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link ServiceResolver}.
 */
class ServiceResolverTest {

    private ServiceResolver resolver;
    private KubernetesServiceDiscoveryProperties properties;

    @BeforeEach
    void setUp() {
        properties = new KubernetesServiceDiscoveryProperties();
        properties.setNamespace("ftgo-dev");
        properties.setClusterDomain("cluster.local");

        KubernetesServiceDiscoveryProperties.ServiceEndpoint orderEndpoint =
                new KubernetesServiceDiscoveryProperties.ServiceEndpoint();
        orderEndpoint.setHost("ftgo-order-service");
        orderEndpoint.setPort(8081);
        properties.getServices().put("order-service", orderEndpoint);

        resolver = new ServiceResolver(properties);
    }

    @Test
    @DisplayName("Resolves service base URL")
    void resolvesServiceBaseUrl() {
        String url = resolver.resolve("order-service");
        assertThat(url).isEqualTo("http://ftgo-order-service.ftgo-dev.svc.cluster.local:8081");
    }

    @Test
    @DisplayName("Resolves service URL with path")
    void resolvesServiceUrlWithPath() {
        String url = resolver.resolve("order-service", "/api/orders");
        assertThat(url).isEqualTo("http://ftgo-order-service.ftgo-dev.svc.cluster.local:8081/api/orders");
    }

    @Test
    @DisplayName("Resolves service URL with path without leading slash")
    void resolvesServiceUrlWithPathNoSlash() {
        String url = resolver.resolve("order-service", "api/orders");
        assertThat(url).isEqualTo("http://ftgo-order-service.ftgo-dev.svc.cluster.local:8081/api/orders");
    }

    @Test
    @DisplayName("Resolves health URL")
    void resolvesHealthUrl() {
        String url = resolver.resolveHealthUrl("order-service");
        assertThat(url).isEqualTo("http://ftgo-order-service.ftgo-dev.svc.cluster.local:8081/actuator/health");
    }

    @Test
    @DisplayName("Returns namespace and cluster domain")
    void returnsNamespaceAndClusterDomain() {
        assertThat(resolver.getNamespace()).isEqualTo("ftgo-dev");
        assertThat(resolver.getClusterDomain()).isEqualTo("cluster.local");
    }

    @Test
    @DisplayName("Falls back to DNS convention for unconfigured services")
    void fallsBackToDnsConvention() {
        String url = resolver.resolve("unknown-service");
        assertThat(url).isEqualTo("http://ftgo-unknown-service.ftgo-dev.svc.cluster.local:8080");
    }
}
