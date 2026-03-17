package net.chrisrichardson.ftgo.security.config;

import net.chrisrichardson.ftgo.security.exception.SecurityExceptionHandlerFilter;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Base Spring Security configuration for all FTGO microservices.
 *
 * <p>Provides:
 * <ul>
 *   <li>Stateless session management (no HTTP sessions)</li>
 *   <li>CSRF disabled for stateless REST APIs</li>
 *   <li>CORS configuration from properties</li>
 *   <li>Public access to health/info actuator endpoints and Swagger UI</li>
 *   <li>All other endpoints require authentication</li>
 *   <li>HTTP Basic authentication (foundation for future JWT/OAuth2)</li>
 *   <li>JSON error responses for 401/403</li>
 * </ul>
 */
@AutoConfiguration
@EnableWebSecurity
@EnableConfigurationProperties(FtgoSecurityProperties.class)
@Import({FtgoCorsConfiguration.class, SecurityExceptionHandlerFilter.class})
public class FtgoBaseSecurityConfiguration {

    private final FtgoSecurityProperties securityProperties;
    private final FtgoCorsConfiguration corsConfiguration;
    private final SecurityExceptionHandlerFilter securityExceptionHandlerFilter;

    public FtgoBaseSecurityConfiguration(FtgoSecurityProperties securityProperties,
                                         FtgoCorsConfiguration corsConfiguration,
                                         SecurityExceptionHandlerFilter securityExceptionHandlerFilter) {
        this.securityProperties = securityProperties;
        this.corsConfiguration = corsConfiguration;
        this.securityExceptionHandlerFilter = securityExceptionHandlerFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        String[] publicPaths = securityProperties.getPublicPaths().toArray(new String[0]);

        http
                // Disable CSRF for stateless REST APIs
                .csrf(AbstractHttpConfigurer::disable)

                // Configure CORS using the CorsConfigurationSource bean
                .cors(cors -> cors.configurationSource(corsConfiguration.corsConfigurationSource()))

                // Stateless session management — no HTTP sessions
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Authorization rules
                .authorizeHttpRequests(authorize -> authorize
                        // Public paths from configuration
                        .requestMatchers(publicPaths).permitAll()
                        // Allow OPTIONS requests for CORS preflight
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        // Secure all actuator endpoints except those in public paths
                        .requestMatchers("/actuator/**").authenticated()
                        // All other requests require authentication
                        .anyRequest().authenticated()
                )

                // HTTP Basic authentication as foundation
                .httpBasic(Customizer.withDefaults())

                // Custom exception handling for JSON error responses
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(new FtgoAuthenticationEntryPoint())
                        .accessDeniedHandler(new FtgoAccessDeniedHandler())
                )

                // Add exception handler filter before authentication
                .addFilterBefore(securityExceptionHandlerFilter,
                        UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
