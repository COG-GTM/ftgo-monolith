package com.ftgo.gateway.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for GatewayProperties defaults and configuration binding.
 */
class GatewayPropertiesTest {

    @Test
    @DisplayName("Should have sensible JWT defaults")
    void shouldHaveSensibleJwtDefaults() {
        GatewayProperties properties = new GatewayProperties();

        assertThat(properties.getJwt().isEnabled()).isTrue();
        assertThat(properties.getJwt().getIssuer()).isEqualTo("ftgo-platform");
        assertThat(properties.getJwt().getExcludedPaths()).isEmpty();
    }

    @Test
    @DisplayName("Should have sensible rate limit defaults")
    void shouldHaveSensibleRateLimitDefaults() {
        GatewayProperties properties = new GatewayProperties();

        assertThat(properties.getRateLimit().isEnabled()).isTrue();
        assertThat(properties.getRateLimit().getDefaultReplenishRate()).isEqualTo(10);
        assertThat(properties.getRateLimit().getDefaultBurstCapacity()).isEqualTo(20);
        assertThat(properties.getRateLimit().getDefaultRequestedTokens()).isEqualTo(1);
        assertThat(properties.getRateLimit().isRedisEnabled()).isFalse();
    }

    @Test
    @DisplayName("Should have sensible CORS defaults")
    void shouldHaveSensibleCorsDefaults() {
        GatewayProperties properties = new GatewayProperties();

        assertThat(properties.getCors().getAllowedOrigins()).contains("*");
        assertThat(properties.getCors().getAllowedMethods()).containsExactlyInAnyOrder(
                "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS");
        assertThat(properties.getCors().getAllowedHeaders()).contains("*");
        assertThat(properties.getCors().isAllowCredentials()).isFalse();
        assertThat(properties.getCors().getMaxAge()).isEqualTo(Duration.ofHours(1));
    }

    @Test
    @DisplayName("Should have sensible versioning defaults")
    void shouldHaveSensibleVersioningDefaults() {
        GatewayProperties properties = new GatewayProperties();

        assertThat(properties.getVersioning().isEnabled()).isTrue();
        assertThat(properties.getVersioning().getHeaderName()).isEqualTo("X-API-Version");
        assertThat(properties.getVersioning().getDefaultVersion()).isEqualTo("v1");
    }

    @Test
    @DisplayName("Should allow configuration of JWT properties")
    void shouldAllowJwtConfiguration() {
        GatewayProperties properties = new GatewayProperties();
        properties.getJwt().setEnabled(false);
        properties.getJwt().setSecret("test-secret");
        properties.getJwt().setIssuer("test-issuer");
        properties.getJwt().setExcludedPaths(List.of("/public/**"));

        assertThat(properties.getJwt().isEnabled()).isFalse();
        assertThat(properties.getJwt().getSecret()).isEqualTo("test-secret");
        assertThat(properties.getJwt().getIssuer()).isEqualTo("test-issuer");
        assertThat(properties.getJwt().getExcludedPaths()).containsExactly("/public/**");
    }

    @Test
    @DisplayName("Should allow configuration of rate limit endpoint overrides")
    void shouldAllowRateLimitEndpointOverrides() {
        GatewayProperties properties = new GatewayProperties();

        GatewayProperties.EndpointRateLimit endpointConfig = new GatewayProperties.EndpointRateLimit();
        endpointConfig.setReplenishRate(50);
        endpointConfig.setBurstCapacity(100);
        properties.getRateLimit().getEndpoints().put("order-service", endpointConfig);

        assertThat(properties.getRateLimit().getEndpoints()).containsKey("order-service");
        GatewayProperties.EndpointRateLimit result = properties.getRateLimit().getEndpoints().get("order-service");
        assertThat(result.getReplenishRate()).isEqualTo(50);
        assertThat(result.getBurstCapacity()).isEqualTo(100);
    }
}
