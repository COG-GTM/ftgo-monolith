package net.chrisrichardson.ftgo.security.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.Collections;

/**
 * CORS configuration for FTGO microservices.
 *
 * <p>Provides a configurable CORS policy via application properties:
 * <ul>
 *   <li>{@code ftgo.security.cors.allowed-origins} - Comma-separated origins (default: *)</li>
 *   <li>{@code ftgo.security.cors.allowed-methods} - Comma-separated HTTP methods</li>
 *   <li>{@code ftgo.security.cors.allowed-headers} - Comma-separated headers</li>
 *   <li>{@code ftgo.security.cors.allow-credentials} - Whether to allow credentials</li>
 *   <li>{@code ftgo.security.cors.max-age} - Pre-flight cache duration in seconds</li>
 * </ul>
 */
@Configuration
public class FtgoCorsConfiguration {

    @Value("${ftgo.security.cors.allowed-origins:*}")
    private String[] allowedOrigins;

    @Value("${ftgo.security.cors.allowed-methods:GET,POST,PUT,DELETE,PATCH,OPTIONS}")
    private String[] allowedMethods;

    @Value("${ftgo.security.cors.allowed-headers:Authorization,Content-Type,X-Requested-With,Accept,Origin}")
    private String[] allowedHeaders;

    @Value("${ftgo.security.cors.allow-credentials:false}")
    private boolean allowCredentials;

    @Value("${ftgo.security.cors.max-age:3600}")
    private long maxAge;

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList(allowedOrigins));
        configuration.setAllowedMethods(Arrays.asList(allowedMethods));
        configuration.setAllowedHeaders(Arrays.asList(allowedHeaders));
        configuration.setAllowCredentials(allowCredentials);
        configuration.setMaxAge(maxAge);
        configuration.setExposedHeaders(Collections.singletonList("Authorization"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
