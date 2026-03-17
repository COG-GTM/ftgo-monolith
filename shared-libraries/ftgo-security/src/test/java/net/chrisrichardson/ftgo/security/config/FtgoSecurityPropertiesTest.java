package net.chrisrichardson.ftgo.security.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for {@link FtgoSecurityProperties} binding and defaults.
 */
@SpringBootTest(classes = TestSecurityApplication.class)
@TestPropertySource(properties = {
        "spring.security.user.name=testuser",
        "spring.security.user.password=testpass"
})
class FtgoSecurityPropertiesTest {

    @Autowired
    private FtgoSecurityProperties properties;

    @Test
    void defaultPublicPathsIncludeHealthAndSwagger() {
        assertNotNull(properties.getPublicPaths());
        assertTrue(properties.getPublicPaths().contains("/actuator/health"));
        assertTrue(properties.getPublicPaths().contains("/actuator/info"));
        assertTrue(properties.getPublicPaths().contains("/swagger-ui/**"));
        assertTrue(properties.getPublicPaths().contains("/v3/api-docs/**"));
    }

    @Test
    void corsOriginsAreConfigurable() {
        FtgoSecurityProperties.Cors cors = properties.getCors();
        assertNotNull(cors);
        assertFalse(cors.getAllowedOrigins().isEmpty());
    }

    @Test
    void defaultCorsAllowsStandardMethods() {
        FtgoSecurityProperties.Cors cors = properties.getCors();
        assertTrue(cors.getAllowedMethods().contains("GET"));
        assertTrue(cors.getAllowedMethods().contains("POST"));
        assertTrue(cors.getAllowedMethods().contains("PUT"));
        assertTrue(cors.getAllowedMethods().contains("DELETE"));
        assertTrue(cors.getAllowedMethods().contains("OPTIONS"));
    }

    @Test
    void defaultCorsDoesNotAllowCredentials() {
        assertFalse(properties.getCors().isAllowCredentials());
    }

    @Test
    void defaultCorsMaxAgeIs3600() {
        assertEquals(3600, properties.getCors().getMaxAge());
    }

    @Test
    void customPublicPathsCanBeSet() {
        FtgoSecurityProperties customProps = new FtgoSecurityProperties();
        customProps.setPublicPaths(java.util.List.of("/custom/**"));
        assertEquals(1, customProps.getPublicPaths().size());
        assertTrue(customProps.getPublicPaths().contains("/custom/**"));
    }
}
