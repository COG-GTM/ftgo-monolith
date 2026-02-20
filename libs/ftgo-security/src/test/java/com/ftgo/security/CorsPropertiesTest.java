package com.ftgo.security;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CorsPropertiesTest {

    @Test
    void defaultAllowedOriginsIsWildcard() {
        CorsProperties properties = new CorsProperties();
        assertThat(properties.getAllowedOrigins()).containsExactly("*");
    }

    @Test
    void defaultAllowedMethodsIncludeStandardHttpMethods() {
        CorsProperties properties = new CorsProperties();
        assertThat(properties.getAllowedMethods())
                .contains("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS");
    }

    @Test
    void defaultAllowCredentialsIsFalse() {
        CorsProperties properties = new CorsProperties();
        assertThat(properties.isAllowCredentials()).isFalse();
    }

    @Test
    void defaultMaxAgeIs3600() {
        CorsProperties properties = new CorsProperties();
        assertThat(properties.getMaxAge()).isEqualTo(3600);
    }

    @Test
    void allowedOriginsAreConfigurable() {
        CorsProperties properties = new CorsProperties();
        properties.setAllowedOrigins(List.of("http://localhost:3000", "https://ftgo.example.com"));
        assertThat(properties.getAllowedOrigins())
                .containsExactly("http://localhost:3000", "https://ftgo.example.com");
    }
}
