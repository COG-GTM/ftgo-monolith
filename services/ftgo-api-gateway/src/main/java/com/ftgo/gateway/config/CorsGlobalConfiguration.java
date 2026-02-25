package com.ftgo.gateway.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

/**
 * Global CORS configuration for the API Gateway.
 * <p>
 * Configured via {@code ftgo.gateway.cors.*} properties. CORS is handled
 * centrally at the gateway so downstream services do not need to configure
 * it individually.
 * </p>
 */
@Configuration
@EnableConfigurationProperties(GatewayProperties.class)
public class CorsGlobalConfiguration {

    private final GatewayProperties gatewayProperties;

    public CorsGlobalConfiguration(GatewayProperties gatewayProperties) {
        this.gatewayProperties = gatewayProperties;
    }

    @Bean
    public CorsWebFilter corsWebFilter() {
        GatewayProperties.Cors corsProps = gatewayProperties.getCors();

        CorsConfiguration corsConfig = new CorsConfiguration();
        corsConfig.setAllowedOrigins(corsProps.getAllowedOrigins());
        corsConfig.setAllowedMethods(corsProps.getAllowedMethods());
        corsConfig.setAllowedHeaders(corsProps.getAllowedHeaders());
        corsConfig.setAllowCredentials(corsProps.isAllowCredentials());
        corsConfig.setMaxAge(corsProps.getMaxAge().getSeconds());

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfig);

        return new CorsWebFilter(source);
    }
}
