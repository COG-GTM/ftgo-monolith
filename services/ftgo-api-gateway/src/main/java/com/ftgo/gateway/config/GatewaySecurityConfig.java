package com.ftgo.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

/**
 * Reactive Spring Security configuration for the API Gateway.
 *
 * <p>Disables default form login and basic auth. JWT validation is handled
 * by the {@link com.ftgo.gateway.filter.JwtValidationGatewayFilter} in the
 * gateway filter chain rather than Spring Security's built-in mechanisms.
 * This allows the gateway to pass validated claims as headers to downstream services.
 */
@Configuration
@EnableWebFluxSecurity
public class GatewaySecurityConfig {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers(
                                "/actuator/**",
                                "/actuator/health/**",
                                "/actuator/info",
                                "/actuator/prometheus"
                        ).permitAll()
                        .anyExchange().permitAll()
                )
                .build();
    }
}
