package net.chrisrichardson.ftgo.security;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Base Spring Security configuration for all FTGO microservices.
 * <p>
 * Provides stateless session management, CSRF disabled for REST APIs,
 * and sensible defaults for actuator endpoint security.
 * <p>
 * Services can override this by defining their own SecurityFilterChain bean.
 */
@AutoConfiguration
@EnableWebSecurity
public class FtgoSecurityConfiguration {

    @Bean
    public SecurityFilterChain ftgoSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            // Stateless session management for microservices
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            // Disable CSRF for stateless REST APIs
            .csrf(csrf -> csrf.disable())

            // Endpoint authorization rules
            .authorizeHttpRequests(auth -> auth
                // Actuator health endpoint is public
                .requestMatchers("/actuator/health", "/actuator/health/**").permitAll()
                // Actuator info endpoint is public
                .requestMatchers("/actuator/info").permitAll()
                // Prometheus metrics endpoint for scraping
                .requestMatchers("/actuator/prometheus").permitAll()
                // OpenAPI/Swagger UI endpoints are public
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()
                // All other actuator endpoints require authentication
                .requestMatchers("/actuator/**").authenticated()
                // All other endpoints require authentication
                .anyRequest().authenticated())

            // CORS configuration
            .cors(cors -> cors.configurationSource(new FtgoCorsConfigurationSource()));

        return http.build();
    }
}
