package net.chrisrichardson.ftgo.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

/**
 * Security configuration for the API Gateway.
 *
 * <p>Disables default Spring Security form login and CSRF (handled by JWT filter).
 * The actual JWT validation is done in {@link net.chrisrichardson.ftgo.gateway.filter.JwtAuthenticationFilter}
 * as a Gateway filter, giving us fine-grained control per route.
 */
@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                .authorizeExchange(exchanges -> exchanges
                        // Health and actuator endpoints are public
                        .pathMatchers("/actuator/**").permitAll()
                        // Fallback endpoints are internal
                        .pathMatchers("/fallback/**").permitAll()
                        // All other requests are handled by the JWT gateway filter
                        .anyExchange().permitAll()
                )
                .build();
    }
}
