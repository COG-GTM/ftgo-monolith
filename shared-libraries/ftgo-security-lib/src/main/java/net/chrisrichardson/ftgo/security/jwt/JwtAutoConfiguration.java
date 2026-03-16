package net.chrisrichardson.ftgo.security.jwt;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Auto-configuration for JWT authentication components.
 *
 * <p>Activated when {@code ftgo.security.jwt.enabled=true} (the default).
 * All beans are conditional and can be overridden by service-specific definitions.
 *
 * <p>Beans provided:
 * <ul>
 *   <li>{@link JwtProperties} — externalized JWT configuration</li>
 *   <li>{@link JwtTokenProvider} — token creation</li>
 *   <li>{@link JwtTokenValidator} — token validation and claims parsing</li>
 *   <li>{@link JwtAuthenticationFilter} — servlet filter for request authentication</li>
 *   <li>{@link JwtRefreshService} — token refresh logic</li>
 * </ul>
 */
@Configuration
@ConditionalOnProperty(prefix = "ftgo.security.jwt", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(JwtProperties.class)
public class JwtAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public JwtTokenProvider jwtTokenProvider(JwtProperties properties) {
        return new JwtTokenProvider(properties);
    }

    @Bean
    @ConditionalOnMissingBean
    public JwtTokenValidator jwtTokenValidator(JwtProperties properties) {
        return new JwtTokenValidator(properties);
    }

    @Bean
    @ConditionalOnMissingBean
    public JwtAuthenticationFilter jwtAuthenticationFilter(JwtTokenValidator validator,
                                                           JwtProperties properties) {
        return new JwtAuthenticationFilter(validator, properties);
    }

    @Bean
    @ConditionalOnMissingBean
    public JwtRefreshService jwtRefreshService(JwtTokenProvider provider,
                                               JwtTokenValidator validator) {
        return new JwtRefreshService(provider, validator);
    }
}
