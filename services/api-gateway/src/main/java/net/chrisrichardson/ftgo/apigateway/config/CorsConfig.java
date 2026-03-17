package net.chrisrichardson.ftgo.apigateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class CorsConfig {

    @Value("${ftgo.gateway.cors.allowed-origins:*}")
    private String allowedOrigins;

    @Value("${ftgo.gateway.cors.allowed-methods:GET,POST,PUT,DELETE,PATCH,OPTIONS}")
    private String allowedMethods;

    @Value("${ftgo.gateway.cors.allowed-headers:*}")
    private String allowedHeaders;

    @Value("${ftgo.gateway.cors.max-age:3600}")
    private long maxAge;

    @Value("${ftgo.gateway.cors.allow-credentials:false}")
    private boolean allowCredentials;

    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration corsConfig = new CorsConfiguration();
        corsConfig.setAllowedOrigins(List.of(allowedOrigins.split(",")));
        corsConfig.setAllowedMethods(List.of(allowedMethods.split(",")));
        corsConfig.setAllowedHeaders(List.of(allowedHeaders.split(",")));
        corsConfig.setMaxAge(maxAge);
        corsConfig.setAllowCredentials(allowCredentials);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", corsConfig);

        return new CorsWebFilter(source);
    }
}
