package com.ftgo.security.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Actuator-specific security configuration.
 *
 * <p>This filter chain is ordered before the main API filter chain and applies
 * only to actuator endpoints ({@code /actuator/**}):
 * <ul>
 *   <li>{@code /actuator/health} and {@code /actuator/info} — publicly accessible</li>
 *   <li>All other actuator endpoints — require authentication</li>
 * </ul>
 *
 * <p>This separation allows actuator endpoints to have different security
 * rules from the main API endpoints.
 */
@Configuration
public class FtgoActuatorSecurityConfig {

    /**
     * SecurityFilterChain specifically for actuator endpoints.
     *
     * <p>Ordered at priority 50 (before the main API chain at 100) to ensure
     * actuator requests are matched first.
     *
     * @param http the HttpSecurity builder
     * @return configured SecurityFilterChain for actuator endpoints
     * @throws Exception if configuration fails
     */
    @Bean
    @Order(50)
    @ConditionalOnMissingBean(name = "ftgoActuatorSecurityFilterChain")
    public SecurityFilterChain ftgoActuatorSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/actuator/**")

                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .csrf(csrf -> csrf.disable())

                .authorizeHttpRequests(authorize -> authorize
                        // Health and info endpoints are always public
                        .requestMatchers("/actuator/health", "/actuator/health/**", "/actuator/info").permitAll()

                        // All other actuator endpoints require authentication
                        .anyRequest().authenticated()
                );

        return http.build();
    }
}
