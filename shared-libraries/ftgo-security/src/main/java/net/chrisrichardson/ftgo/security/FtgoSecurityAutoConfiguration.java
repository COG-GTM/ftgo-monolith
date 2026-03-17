package net.chrisrichardson.ftgo.security;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * Auto-configuration for FTGO microservice security.
 *
 * <p>Provides a base {@link SecurityFilterChain} with:
 * <ul>
 *   <li>Stateless session management (no HTTP sessions)</li>
 *   <li>CSRF disabled (stateless REST APIs)</li>
 *   <li>CORS configured via {@link FtgoSecurityProperties}</li>
 *   <li>Actuator {@code /health} endpoint publicly accessible</li>
 *   <li>All other endpoints require authentication</li>
 * </ul>
 *
 * <p>Can be disabled with {@code ftgo.security.enabled=false}.
 */
@AutoConfiguration
@ConditionalOnWebApplication
@ConditionalOnProperty(prefix = "ftgo.security", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableWebSecurity
@EnableConfigurationProperties(FtgoSecurityProperties.class)
@Import(SecurityExceptionHandler.class)
public class FtgoSecurityAutoConfiguration {

    private final FtgoSecurityProperties securityProperties;

    public FtgoSecurityAutoConfiguration(FtgoSecurityProperties securityProperties) {
        this.securityProperties = securityProperties;
    }

    /**
     * Configures the base security filter chain for FTGO microservices.
     */
    @Bean
    public SecurityFilterChain ftgoSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            // Disable CSRF for stateless REST APIs
            .csrf(AbstractHttpConfigurer::disable)

            // Stateless session management - no HTTP sessions
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            // CORS configuration
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))

            // Authorization rules
            .authorizeHttpRequests(authorize -> {
                // Actuator health endpoint is public
                authorize.requestMatchers("/actuator/health", "/actuator/health/**").permitAll();

                // Additional public paths from configuration
                String[] publicPaths = securityProperties.getPublicPaths().toArray(new String[0]);
                if (publicPaths.length > 0) {
                    authorize.requestMatchers(publicPaths).permitAll();
                }

                // All other endpoints require authentication
                authorize.anyRequest().authenticated();
            })

            // Use HTTP Basic as the default authentication mechanism
            .httpBasic(basic -> basic
                .authenticationEntryPoint(new FtgoAuthenticationEntryPoint())
            );

        return http.build();
    }

    /**
     * Configures CORS based on application properties.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        FtgoSecurityProperties.Cors corsProps = securityProperties.getCors();
        configuration.setAllowedOrigins(corsProps.getAllowedOrigins());
        configuration.setAllowedMethods(corsProps.getAllowedMethods());
        configuration.setAllowedHeaders(corsProps.getAllowedHeaders());
        configuration.setAllowCredentials(corsProps.isAllowCredentials());
        configuration.setMaxAge(corsProps.getMaxAge());

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
