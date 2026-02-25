package com.ftgo.gateway.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

/**
 * Security configuration for the reactive (WebFlux) API Gateway.
 * <p>
 * Since Spring Cloud Gateway is Netty/WebFlux-based, we use reactive
 * security configuration instead of the servlet-based configuration
 * from ftgo-security-lib. JWT validation is handled by our custom
 * {@link com.ftgo.gateway.filter.JwtAuthenticationGatewayFilterFactory}
 * which runs as a Gateway filter rather than a Spring Security filter.
 * </p>
 */
@Configuration
@EnableWebFluxSecurity
@EnableConfigurationProperties(GatewayProperties.class)
public class GatewaySecurityConfiguration {

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                // We permit all requests at the Spring Security level;
                // JWT validation is handled per-route by the gateway filter.
                .authorizeExchange(exchanges -> exchanges
                        .anyExchange().permitAll())
                .build();
    }
}
