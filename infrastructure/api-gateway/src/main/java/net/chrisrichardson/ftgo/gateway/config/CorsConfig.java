package net.chrisrichardson.ftgo.gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Global CORS configuration for the API Gateway.
 *
 * <p>Centralizes CORS handling at the gateway level so individual
 * microservices do not need to configure CORS independently.
 *
 * <p>Note: When {@code allowCredentials} is {@code true}, wildcard origins
 * are automatically converted to {@code allowedOriginPatterns} to comply
 * with the CORS specification (credentials + wildcard origin is invalid).
 */
@Configuration
public class CorsConfig {

    @Value("${ftgo.gateway.cors.allowed-origins:*}")
    private String allowedOrigins;

    @Value("${ftgo.gateway.cors.allowed-methods:GET,POST,PUT,DELETE,PATCH,OPTIONS}")
    private String allowedMethods;

    @Value("${ftgo.gateway.cors.allowed-headers:*}")
    private String allowedHeaders;

    @Value("${ftgo.gateway.cors.exposed-headers:X-Correlation-Id,X-RateLimit-Remaining}")
    private String exposedHeaders;

    @Value("${ftgo.gateway.cors.allow-credentials:false}")
    private boolean allowCredentials;

    @Value("${ftgo.gateway.cors.max-age:3600}")
    private long maxAge;

    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration corsConfig = new CorsConfiguration();

        // When credentials are enabled and origins contain "*", use allowedOriginPatterns
        // to comply with CORS spec (credentials + wildcard origin is invalid)
        List<String> origins = List.of(allowedOrigins.split(","));
        if (allowCredentials && origins.contains("*")) {
            corsConfig.setAllowedOriginPatterns(List.of("*"));
        } else {
            corsConfig.setAllowedOrigins(origins);
        }

        corsConfig.setAllowedMethods(List.of(allowedMethods.split(",")));
        corsConfig.setAllowedHeaders(List.of(allowedHeaders.split(",")));
        corsConfig.setExposedHeaders(List.of(exposedHeaders.split(",")));
        corsConfig.setAllowCredentials(allowCredentials);
        corsConfig.setMaxAge(maxAge);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfig);

        return new CorsWebFilter(source);
    }
}
