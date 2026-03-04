package com.ftgo.security.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * CORS configuration for FTGO microservices.
 *
 * <p>Configures Cross-Origin Resource Sharing to allow requests from
 * the API gateway and other trusted origins. Origins are configurable
 * via application properties.
 *
 * <p>Properties:
 * <ul>
 *   <li>{@code ftgo.security.cors.allowed-origins} — comma-separated list of allowed origins
 *       (default: {@code http://localhost:3000,http://localhost:8080})</li>
 *   <li>{@code ftgo.security.cors.allowed-methods} — comma-separated list of allowed HTTP methods
 *       (default: {@code GET,POST,PUT,DELETE,PATCH,OPTIONS})</li>
 *   <li>{@code ftgo.security.cors.allowed-headers} — comma-separated list of allowed headers
 *       (default: {@code *})</li>
 *   <li>{@code ftgo.security.cors.max-age} — max age for pre-flight cache in seconds
 *       (default: {@code 3600})</li>
 * </ul>
 */
@Configuration
public class FtgoCorsConfig {

    @Value("${ftgo.security.cors.allowed-origins:http://localhost:3000,http://localhost:8080}")
    private String[] allowedOrigins;

    @Value("${ftgo.security.cors.allowed-methods:GET,POST,PUT,DELETE,PATCH,OPTIONS}")
    private String[] allowedMethods;

    @Value("${ftgo.security.cors.allowed-headers:*}")
    private String[] allowedHeaders;

    @Value("${ftgo.security.cors.max-age:3600}")
    private long maxAge;

    /**
     * Provides CORS configuration source for use with Spring Security.
     *
     * <p>This bean is automatically picked up by Spring Security's CORS filter
     * when {@code .cors(Customizer.withDefaults())} is configured.
     *
     * @return the CORS configuration source
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList(allowedOrigins));
        configuration.setAllowedMethods(Arrays.asList(allowedMethods));
        configuration.setAllowedHeaders(Arrays.asList(allowedHeaders));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(maxAge);
        configuration.setExposedHeaders(List.of("Authorization", "Content-Type"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
