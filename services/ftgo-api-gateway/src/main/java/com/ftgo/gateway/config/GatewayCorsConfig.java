package com.ftgo.gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Global CORS configuration for the API Gateway.
 *
 * <p>Provides a reactive {@link CorsWebFilter} that applies CORS headers
 * to all gateway responses. This centralizes CORS handling at the gateway
 * level so downstream services do not need individual CORS configuration.
 *
 * <p>Properties:
 * <ul>
 *   <li>{@code ftgo.gateway.cors.allowed-origins} — comma-separated allowed origins</li>
 *   <li>{@code ftgo.gateway.cors.allowed-methods} — comma-separated allowed HTTP methods</li>
 *   <li>{@code ftgo.gateway.cors.allowed-headers} — comma-separated allowed headers</li>
 *   <li>{@code ftgo.gateway.cors.exposed-headers} — comma-separated exposed headers</li>
 *   <li>{@code ftgo.gateway.cors.max-age} — max age for pre-flight cache (seconds)</li>
 *   <li>{@code ftgo.gateway.cors.allow-credentials} — whether credentials are allowed</li>
 * </ul>
 */
@Configuration
public class GatewayCorsConfig {

    @Value("${ftgo.gateway.cors.allowed-origins:http://localhost:3000,http://localhost:8080}")
    private String[] allowedOrigins;

    @Value("${ftgo.gateway.cors.allowed-methods:GET,POST,PUT,DELETE,PATCH,OPTIONS}")
    private String[] allowedMethods;

    @Value("${ftgo.gateway.cors.allowed-headers:*}")
    private String[] allowedHeaders;

    @Value("${ftgo.gateway.cors.exposed-headers:Authorization,Content-Type,X-Correlation-ID,X-API-Version}")
    private String[] exposedHeaders;

    @Value("${ftgo.gateway.cors.max-age:3600}")
    private long maxAge;

    @Value("${ftgo.gateway.cors.allow-credentials:true}")
    private boolean allowCredentials;

    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList(allowedOrigins));
        configuration.setAllowedMethods(Arrays.asList(allowedMethods));
        configuration.setAllowedHeaders(Arrays.asList(allowedHeaders));
        configuration.setExposedHeaders(Arrays.asList(exposedHeaders));
        configuration.setAllowCredentials(allowCredentials);
        configuration.setMaxAge(maxAge);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return new CorsWebFilter(source);
    }
}
