package net.chrisrichardson.ftgo.security;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for {@link FtgoSecurityProperties}.
 */
public class FtgoSecurityPropertiesTest {

    @Test
    public void shouldHaveDefaultPublicPaths() {
        FtgoSecurityProperties properties = new FtgoSecurityProperties();

        List<String> publicPaths = properties.getPublicPaths();
        assertNotNull(publicPaths);
        assertTrue(publicPaths.contains("/actuator/health"));
        assertTrue(publicPaths.contains("/actuator/health/**"));
        assertTrue(publicPaths.contains("/actuator/info"));
    }

    @Test
    public void shouldReturnPublicPathsAsArray() {
        FtgoSecurityProperties properties = new FtgoSecurityProperties();

        String[] paths = properties.getPublicPathsArray();
        assertNotNull(paths);
        assertEquals(3, paths.length);
    }

    @Test
    public void shouldAllowSettingCustomPublicPaths() {
        FtgoSecurityProperties properties = new FtgoSecurityProperties();
        List<String> customPaths = Arrays.asList("/api/public/**", "/swagger-ui.html");
        properties.setPublicPaths(customPaths);

        assertEquals(customPaths, properties.getPublicPaths());
        assertArrayEquals(new String[]{"/api/public/**", "/swagger-ui.html"},
                properties.getPublicPathsArray());
    }

    @Test
    public void shouldHaveDefaultCorsConfiguration() {
        FtgoSecurityProperties properties = new FtgoSecurityProperties();
        FtgoSecurityProperties.Cors cors = properties.getCors();

        assertNotNull(cors);
        assertTrue(cors.isEnabled());
        assertTrue(cors.getAllowedOrigins().contains("http://localhost:3000"));
        assertTrue(cors.getAllowedOrigins().contains("http://localhost:8080"));
        assertTrue(cors.getAllowedMethods().contains("GET"));
        assertTrue(cors.getAllowedMethods().contains("POST"));
        assertTrue(cors.getAllowedMethods().contains("PUT"));
        assertTrue(cors.getAllowedMethods().contains("DELETE"));
        assertTrue(cors.getAllowedHeaders().contains("Authorization"));
        assertTrue(cors.getAllowedHeaders().contains("Content-Type"));
        assertTrue(cors.isAllowCredentials());
        assertEquals(3600L, cors.getMaxAge());
        assertEquals("/**", cors.getPattern());
    }

    @Test
    public void shouldAllowCustomCorsOrigins() {
        FtgoSecurityProperties properties = new FtgoSecurityProperties();
        FtgoSecurityProperties.Cors cors = properties.getCors();

        List<String> origins = Arrays.asList("https://api-gateway.ftgo.com", "https://www.ftgo.com");
        cors.setAllowedOrigins(origins);

        assertEquals(origins, cors.getAllowedOrigins());
    }

    @Test
    public void shouldAllowCustomCorsSettings() {
        FtgoSecurityProperties properties = new FtgoSecurityProperties();
        FtgoSecurityProperties.Cors cors = properties.getCors();

        cors.setEnabled(false);
        cors.setAllowCredentials(false);
        cors.setMaxAge(7200L);
        cors.setPattern("/api/**");

        assertEquals(false, cors.isEnabled());
        assertEquals(false, cors.isAllowCredentials());
        assertEquals(7200L, cors.getMaxAge());
        assertEquals("/api/**", cors.getPattern());
    }

    @Test
    public void shouldHaveDefaultActuatorConfiguration() {
        FtgoSecurityProperties properties = new FtgoSecurityProperties();
        FtgoSecurityProperties.Actuator actuator = properties.getActuator();

        assertNotNull(actuator);
        assertTrue(actuator.getPublicEndpoints().contains("health"));
        assertTrue(actuator.getPublicEndpoints().contains("info"));
    }

    @Test
    public void shouldAllowCustomActuatorPublicEndpoints() {
        FtgoSecurityProperties properties = new FtgoSecurityProperties();
        FtgoSecurityProperties.Actuator actuator = properties.getActuator();

        List<String> endpoints = Arrays.asList("health", "info", "prometheus");
        actuator.setPublicEndpoints(endpoints);

        assertEquals(endpoints, actuator.getPublicEndpoints());
    }

    @Test
    public void shouldAllowSettingExposedHeaders() {
        FtgoSecurityProperties properties = new FtgoSecurityProperties();
        FtgoSecurityProperties.Cors cors = properties.getCors();

        List<String> headers = Arrays.asList("Authorization", "X-Custom-Header");
        cors.setExposedHeaders(headers);

        assertEquals(headers, cors.getExposedHeaders());
    }

    @Test
    public void shouldAllowSettingAllowedMethods() {
        FtgoSecurityProperties properties = new FtgoSecurityProperties();
        FtgoSecurityProperties.Cors cors = properties.getCors();

        List<String> methods = Arrays.asList("GET", "POST");
        cors.setAllowedMethods(methods);

        assertEquals(methods, cors.getAllowedMethods());
    }

    @Test
    public void shouldAllowSettingAllowedHeaders() {
        FtgoSecurityProperties properties = new FtgoSecurityProperties();
        FtgoSecurityProperties.Cors cors = properties.getCors();

        List<String> headers = Arrays.asList("Authorization");
        cors.setAllowedHeaders(headers);

        assertEquals(headers, cors.getAllowedHeaders());
    }
}
