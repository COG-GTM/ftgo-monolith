package com.ftgo.security.config;

import com.ftgo.security.handler.FtgoAccessDeniedHandler;
import com.ftgo.security.handler.FtgoAuthenticationEntryPoint;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Base Spring Security configuration for FTGO microservices.
 *
 * <p>Establishes the following security posture:
 * <ul>
 *   <li>Stateless session management — no HTTP sessions are created or used</li>
 *   <li>CSRF protection disabled — appropriate for stateless REST APIs</li>
 *   <li>Public endpoints: health check, OpenAPI docs</li>
 *   <li>All other endpoints require authentication</li>
 *   <li>Custom JSON error responses for 401 and 403</li>
 * </ul>
 *
 * <p>Services can override this bean by defining their own {@code SecurityFilterChain}
 * with the same bean name or a higher {@code @Order} priority.
 */
@Configuration
@EnableWebSecurity
public class FtgoBaseSecurityConfig {

    private final FtgoAuthenticationEntryPoint authenticationEntryPoint;
    private final FtgoAccessDeniedHandler accessDeniedHandler;

    public FtgoBaseSecurityConfig(FtgoAuthenticationEntryPoint authenticationEntryPoint,
                                  FtgoAccessDeniedHandler accessDeniedHandler) {
        this.authenticationEntryPoint = authenticationEntryPoint;
        this.accessDeniedHandler = accessDeniedHandler;
    }

    /**
     * Primary SecurityFilterChain for API endpoints.
     *
     * <p>This filter chain applies to all requests and configures:
     * <ul>
     *   <li>Stateless sessions (SessionCreationPolicy.STATELESS)</li>
     *   <li>CSRF disabled for REST APIs</li>
     *   <li>Public access to health, info, and OpenAPI endpoints</li>
     *   <li>Authentication required for all other endpoints</li>
     * </ul>
     *
     * @param http the HttpSecurity builder
     * @return configured SecurityFilterChain
     * @throws Exception if configuration fails
     */
    @Bean
    @Order(100)
    @ConditionalOnMissingBean(name = "ftgoApiSecurityFilterChain")
    public SecurityFilterChain ftgoApiSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                // Stateless session management — no HTTP sessions
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Disable CSRF — not needed for stateless REST APIs
                .csrf(csrf -> csrf.disable())

                // Enable CORS using the CorsConfigurationSource bean
                .cors(Customizer.withDefaults())

                // Authorization rules
                .authorizeHttpRequests(authorize -> authorize
                        // Public health and info endpoints
                        .requestMatchers("/actuator/health", "/actuator/health/**", "/actuator/info").permitAll()

                        // Public OpenAPI / Swagger endpoints
                        .requestMatchers(
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/swagger-resources/**"
                        ).permitAll()

                        // All other requests require authentication
                        .anyRequest().authenticated()
                )

                // Custom error handling
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler)
                );

        return http.build();
    }
}
