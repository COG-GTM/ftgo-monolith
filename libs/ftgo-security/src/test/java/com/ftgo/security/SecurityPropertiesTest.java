package com.ftgo.security;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SecurityPropertiesTest {

    @Test
    void defaultCsrfIsDisabled() {
        SecurityProperties properties = new SecurityProperties();
        assertThat(properties.isCsrfEnabled()).isFalse();
    }

    @Test
    void defaultPublicPathsIncludeActuatorAndSwagger() {
        SecurityProperties properties = new SecurityProperties();
        assertThat(properties.getPublicPaths())
                .contains("/actuator/**", "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html");
    }

    @Test
    void publicPathsAreConfigurable() {
        SecurityProperties properties = new SecurityProperties();
        properties.setPublicPaths(List.of("/custom/**"));
        assertThat(properties.getPublicPaths()).containsExactly("/custom/**");
    }
}
