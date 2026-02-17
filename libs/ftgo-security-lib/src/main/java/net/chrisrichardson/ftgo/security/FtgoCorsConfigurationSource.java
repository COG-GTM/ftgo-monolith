package net.chrisrichardson.ftgo.security;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * CORS configuration for FTGO microservices.
 * Allows the API gateway origin and standard HTTP methods.
 */
public class FtgoCorsConfigurationSource implements CorsConfigurationSource {

    @Override
    public CorsConfiguration getCorsConfiguration(HttpServletRequest request) {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(List.of("*"));
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Requested-With", "Accept", "Origin"));
        config.setExposedHeaders(Arrays.asList("Authorization", "X-Request-Id"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);
        return config;
    }
}
