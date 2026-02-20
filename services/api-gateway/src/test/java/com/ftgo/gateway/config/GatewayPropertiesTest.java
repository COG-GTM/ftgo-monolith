package com.ftgo.gateway.config;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GatewayPropertiesTest {

    @Test
    void shouldHaveDefaultValues() {
        GatewayProperties properties = new GatewayProperties();

        assertThat(properties.getRoutes()).isEmpty();
        assertThat(properties.getDefaultRateLimit()).isNotNull();
        assertThat(properties.getDefaultRateLimit().getRequestsPerSecond()).isEqualTo(100);
        assertThat(properties.getDefaultRateLimit().getBurstCapacity()).isEqualTo(150);
    }

    @Test
    void shouldSetAndGetRoutes() {
        GatewayProperties properties = new GatewayProperties();

        GatewayProperties.ServiceRoute route = new GatewayProperties.ServiceRoute();
        route.setId("order-service");
        route.setPath("/api/orders/**");
        route.setUri("http://localhost:8081");
        route.setStripPrefix(1);
        route.setAuthRequired(true);
        route.setRateLimit(50);
        route.setBurstCapacity(100);
        route.setRequiredRoles(List.of("ROLE_USER"));

        properties.setRoutes(List.of(route));

        assertThat(properties.getRoutes()).hasSize(1);
        assertThat(properties.getRoutes().get(0).getId()).isEqualTo("order-service");
        assertThat(properties.getRoutes().get(0).getPath()).isEqualTo("/api/orders/**");
        assertThat(properties.getRoutes().get(0).getUri()).isEqualTo("http://localhost:8081");
        assertThat(properties.getRoutes().get(0).getStripPrefix()).isEqualTo(1);
        assertThat(properties.getRoutes().get(0).isAuthRequired()).isTrue();
        assertThat(properties.getRoutes().get(0).getRateLimit()).isEqualTo(50);
        assertThat(properties.getRoutes().get(0).getBurstCapacity()).isEqualTo(100);
        assertThat(properties.getRoutes().get(0).getRequiredRoles()).containsExactly("ROLE_USER");
    }

    @Test
    void shouldSetAndGetCorsProperties() {
        GatewayProperties properties = new GatewayProperties();

        GatewayProperties.Cors cors = new GatewayProperties.Cors();
        cors.setAllowedOrigins(List.of("http://example.com"));
        cors.setAllowedMethods(List.of("GET", "POST"));
        cors.setAllowedHeaders(List.of("Content-Type"));
        cors.setExposedHeaders(List.of("X-Custom-Header"));
        cors.setAllowCredentials(false);
        cors.setMaxAge(7200);

        properties.setCors(cors);

        assertThat(properties.getCors().getAllowedOrigins()).containsExactly("http://example.com");
        assertThat(properties.getCors().getAllowedMethods()).containsExactly("GET", "POST");
        assertThat(properties.getCors().getAllowedHeaders()).containsExactly("Content-Type");
        assertThat(properties.getCors().getExposedHeaders()).containsExactly("X-Custom-Header");
        assertThat(properties.getCors().isAllowCredentials()).isFalse();
        assertThat(properties.getCors().getMaxAge()).isEqualTo(7200);
    }

    @Test
    void shouldHaveDefaultCorsValues() {
        GatewayProperties.Cors cors = new GatewayProperties.Cors();

        assertThat(cors.getAllowedOrigins()).containsExactly("http://localhost:3000");
        assertThat(cors.getAllowedMethods()).contains("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS");
        assertThat(cors.getAllowedHeaders()).containsExactly("*");
        assertThat(cors.getExposedHeaders()).containsExactly("X-Correlation-Id");
        assertThat(cors.isAllowCredentials()).isTrue();
        assertThat(cors.getMaxAge()).isEqualTo(3600);
    }

    @Test
    void shouldSetAndGetRateLimitProperties() {
        GatewayProperties.RateLimit rateLimit = new GatewayProperties.RateLimit();
        rateLimit.setRequestsPerSecond(200);
        rateLimit.setBurstCapacity(300);

        assertThat(rateLimit.getRequestsPerSecond()).isEqualTo(200);
        assertThat(rateLimit.getBurstCapacity()).isEqualTo(300);
    }

    @Test
    void shouldHaveDefaultServiceRouteValues() {
        GatewayProperties.ServiceRoute route = new GatewayProperties.ServiceRoute();

        assertThat(route.getStripPrefix()).isEqualTo(1);
        assertThat(route.isAuthRequired()).isTrue();
        assertThat(route.getRequiredRoles()).isEmpty();
        assertThat(route.getRateLimit()).isEqualTo(100);
        assertThat(route.getBurstCapacity()).isEqualTo(150);
    }
}
