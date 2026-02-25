package com.ftgo.security.config;

import com.ftgo.security.exception.FtgoAccessDeniedHandler;
import com.ftgo.security.exception.FtgoAuthenticationEntryPoint;
import com.ftgo.security.jwt.JwtAuthenticationFilter;
import com.ftgo.security.properties.FtgoSecurityProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import java.util.List;

/**
 * Base Spring Security configuration for all FTGO microservices.
 * <p>
 * Provides a secure-by-default configuration with:
 * <ul>
 *   <li>Stateless session management (no HTTP sessions)</li>
 *   <li>CSRF disabled (stateless REST APIs)</li>
 *   <li>CORS configured via {@link FtgoCorsConfiguration}</li>
 *   <li>Public access to health and info actuator endpoints</li>
 *   <li>All other endpoints require authentication</li>
 *   <li>HTTP Basic authentication enabled for service-to-service calls</li>
 *   <li>Custom JSON error responses for 401/403</li>
 * </ul>
 * </p>
 * <p>
 * Microservices can override this configuration by defining their own
 * {@link SecurityFilterChain} bean with a higher {@link Order} priority.
 * </p>
 */
@Configuration
@EnableWebSecurity
@EnableConfigurationProperties(FtgoSecurityProperties.class)
public class FtgoBaseSecurityConfiguration {

    private final FtgoSecurityProperties securityProperties;
    private final FtgoAuthenticationEntryPoint authenticationEntryPoint;
    private final FtgoAccessDeniedHandler accessDeniedHandler;

    /**
     * Optional JWT authentication filter. Injected only when
     * {@code ftgo.security.jwt.enabled=true} (see {@link com.ftgo.security.jwt.JwtAutoConfiguration}).
     * When absent, the configuration falls back to HTTP Basic authentication.
     */
    @Autowired(required = false)
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    public FtgoBaseSecurityConfiguration(
            FtgoSecurityProperties securityProperties,
            FtgoAuthenticationEntryPoint authenticationEntryPoint,
            FtgoAccessDeniedHandler accessDeniedHandler) {
        this.securityProperties = securityProperties;
        this.authenticationEntryPoint = authenticationEntryPoint;
        this.accessDeniedHandler = accessDeniedHandler;
    }

    /**
     * Actuator security filter chain.
     * <p>
     * Secures actuator endpoints: /health and /info are public,
     * all other actuator endpoints require authentication.
     * </p>
     */
    @Bean
    @Order(1)
    public SecurityFilterChain actuatorSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            .securityMatcher(new AntPathRequestMatcher("/actuator/**"))
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(authorize -> authorize
                .requestMatchers(
                    new AntPathRequestMatcher("/actuator/health"),
                    new AntPathRequestMatcher("/actuator/health/**"),
                    new AntPathRequestMatcher("/actuator/info")
                ).permitAll()
                .anyRequest().authenticated())
            .httpBasic(Customizer.withDefaults())
            .exceptionHandling(exceptions -> exceptions
                .authenticationEntryPoint(authenticationEntryPoint)
                .accessDeniedHandler(accessDeniedHandler));

        return http.build();
    }

    /**
     * Default API security filter chain.
     * <p>
     * All REST endpoints require authentication by default.
     * Services can customize by adding paths to {@code ftgo.security.public-paths}.
     * </p>
     */
    @Bean
    @Order(2)
    public SecurityFilterChain apiSecurityFilterChain(HttpSecurity http) throws Exception {
        String[] publicPaths = securityProperties.getPublicPaths()
                .toArray(new String[0]);

        List<AntPathRequestMatcher> publicMatchers = securityProperties.getPublicPaths().stream()
                .map(AntPathRequestMatcher::new)
                .toList();

        http
            .cors(Customizer.withDefaults())
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(authorize -> {
                if (!publicMatchers.isEmpty()) {
                    authorize.requestMatchers(
                        publicMatchers.toArray(new AntPathRequestMatcher[0])
                    ).permitAll();
                }
                authorize.anyRequest().authenticated();
            })
            .exceptionHandling(exceptions -> exceptions
                .authenticationEntryPoint(authenticationEntryPoint)
                .accessDeniedHandler(accessDeniedHandler));

        // When JWT is enabled, register the JWT filter before the
        // UsernamePasswordAuthenticationFilter. Otherwise fall back
        // to HTTP Basic authentication (EM-39 default).
        if (jwtAuthenticationFilter != null) {
            http.addFilterBefore(jwtAuthenticationFilter,
                    UsernamePasswordAuthenticationFilter.class);
        } else {
            http.httpBasic(Customizer.withDefaults());
        }

        return http.build();
    }
}
