package com.ftgo.security.jwt;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Auto-configuration for JWT authentication components.
 * <p>
 * Activated only when {@code ftgo.security.jwt.enabled=true}.
 * Registers:
 * <ul>
 *   <li>{@link JwtTokenProvider} – token generation and validation</li>
 *   <li>{@link JwtTokenService} – high-level token lifecycle management</li>
 *   <li>{@link JwtAuthenticationFilter} – servlet filter for Bearer tokens</li>
 * </ul>
 * </p>
 *
 * @see JwtProperties
 */
@Configuration
@EnableConfigurationProperties(JwtProperties.class)
@ConditionalOnProperty(name = "ftgo.security.jwt.enabled", havingValue = "true")
public class JwtAutoConfiguration {

    @Bean
    public JwtTokenProvider jwtTokenProvider(JwtProperties jwtProperties) {
        return new JwtTokenProvider(jwtProperties);
    }

    @Bean
    public JwtTokenService jwtTokenService(JwtTokenProvider tokenProvider,
                                           JwtProperties jwtProperties) {
        return new JwtTokenService(tokenProvider, jwtProperties);
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter(JwtTokenProvider tokenProvider) {
        return new JwtAuthenticationFilter(tokenProvider);
    }
}
