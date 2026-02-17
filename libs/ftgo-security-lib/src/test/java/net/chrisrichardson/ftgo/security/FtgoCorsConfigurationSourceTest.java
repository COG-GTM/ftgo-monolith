package net.chrisrichardson.ftgo.security;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.cors.CorsConfiguration;

import static org.junit.jupiter.api.Assertions.*;

class FtgoCorsConfigurationSourceTest {

    private final FtgoCorsConfigurationSource source = new FtgoCorsConfigurationSource();

    @Test
    void shouldAllowAllOriginPatterns() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        CorsConfiguration config = source.getCorsConfiguration(request);

        assertNotNull(config);
        assertNotNull(config.getAllowedOriginPatterns());
        assertTrue(config.getAllowedOriginPatterns().contains("*"));
    }

    @Test
    void shouldAllowStandardHttpMethods() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        CorsConfiguration config = source.getCorsConfiguration(request);

        assertNotNull(config.getAllowedMethods());
        assertTrue(config.getAllowedMethods().contains("GET"));
        assertTrue(config.getAllowedMethods().contains("POST"));
        assertTrue(config.getAllowedMethods().contains("PUT"));
        assertTrue(config.getAllowedMethods().contains("PATCH"));
        assertTrue(config.getAllowedMethods().contains("DELETE"));
        assertTrue(config.getAllowedMethods().contains("OPTIONS"));
    }

    @Test
    void shouldAllowRequiredHeaders() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        CorsConfiguration config = source.getCorsConfiguration(request);

        assertNotNull(config.getAllowedHeaders());
        assertTrue(config.getAllowedHeaders().contains("Authorization"));
        assertTrue(config.getAllowedHeaders().contains("Content-Type"));
    }

    @Test
    void shouldExposeAuthorizationHeader() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        CorsConfiguration config = source.getCorsConfiguration(request);

        assertNotNull(config.getExposedHeaders());
        assertTrue(config.getExposedHeaders().contains("Authorization"));
        assertTrue(config.getExposedHeaders().contains("X-Request-Id"));
    }

    @Test
    void shouldAllowCredentials() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        CorsConfiguration config = source.getCorsConfiguration(request);

        assertTrue(config.getAllowCredentials());
    }

    @Test
    void shouldSetMaxAge() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        CorsConfiguration config = source.getCorsConfiguration(request);

        assertEquals(3600L, config.getMaxAge());
    }
}
