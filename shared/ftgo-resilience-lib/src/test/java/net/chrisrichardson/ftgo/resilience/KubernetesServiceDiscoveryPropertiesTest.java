package net.chrisrichardson.ftgo.resilience;

import net.chrisrichardson.ftgo.resilience.discovery.KubernetesServiceDiscoveryProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link KubernetesServiceDiscoveryProperties}.
 */
class KubernetesServiceDiscoveryPropertiesTest {

    private KubernetesServiceDiscoveryProperties properties;

    @BeforeEach
    void setUp() {
        properties = new KubernetesServiceDiscoveryProperties();
        properties.setNamespace("ftgo-dev");
        properties.setClusterDomain("cluster.local");
    }

    @Test
    @DisplayName("Resolves service URL using K8s DNS convention")
    void resolvesServiceUrlUsingDns() {
        String url = properties.resolveServiceUrl("order-service");

        assertThat(url).isEqualTo("http://ftgo-order-service.ftgo-dev.svc.cluster.local:8080");
    }

    @Test
    @DisplayName("Resolves service URL with configured endpoint")
    void resolvesServiceUrlWithConfiguredEndpoint() {
        KubernetesServiceDiscoveryProperties.ServiceEndpoint endpoint =
                new KubernetesServiceDiscoveryProperties.ServiceEndpoint();
        endpoint.setHost("ftgo-order-service");
        endpoint.setPort(8081);
        properties.getServices().put("order-service", endpoint);

        String url = properties.resolveServiceUrl("order-service");

        assertThat(url).isEqualTo("http://ftgo-order-service.ftgo-dev.svc.cluster.local:8081");
    }

    @Test
    @DisplayName("Resolves service URL with direct URL override")
    void resolvesServiceUrlWithDirectOverride() {
        KubernetesServiceDiscoveryProperties.ServiceEndpoint endpoint =
                new KubernetesServiceDiscoveryProperties.ServiceEndpoint();
        endpoint.setUrl("http://localhost:8081");
        properties.getServices().put("order-service", endpoint);

        String url = properties.resolveServiceUrl("order-service");

        assertThat(url).isEqualTo("http://localhost:8081");
    }

    @Test
    @DisplayName("Uses custom namespace and cluster domain")
    void usesCustomNamespaceAndDomain() {
        properties.setNamespace("ftgo-prod");
        properties.setClusterDomain("custom.cluster");

        String url = properties.resolveServiceUrl("consumer-service");

        assertThat(url).isEqualTo("http://ftgo-consumer-service.ftgo-prod.svc.custom.cluster:8080");
    }

    @Test
    @DisplayName("Supports HTTPS scheme")
    void supportsHttpsScheme() {
        KubernetesServiceDiscoveryProperties.ServiceEndpoint endpoint =
                new KubernetesServiceDiscoveryProperties.ServiceEndpoint();
        endpoint.setHost("ftgo-order-service");
        endpoint.setPort(8443);
        endpoint.setScheme("https");
        properties.getServices().put("order-service", endpoint);

        String url = properties.resolveServiceUrl("order-service");

        assertThat(url).isEqualTo("https://ftgo-order-service.ftgo-dev.svc.cluster.local:8443");
    }
}
