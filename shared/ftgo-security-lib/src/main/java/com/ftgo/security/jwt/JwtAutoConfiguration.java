package com.ftgo.security.jwt;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Auto-configuration for JWT authentication components.
 *
 * <p>This configuration is activated when {@code ftgo.security.jwt.enabled=true}
 * (the default). It registers:
 * <ul>
 *   <li>{@link FtgoJwtProperties} — JWT configuration properties</li>
 *   <li>{@link JwtTokenProvider} — token creation/validation</li>
 *   <li>{@link JwtAuthenticationFilter} — request filter for Bearer token extraction</li>
 * </ul>
 *
 * <p>To disable JWT authentication entirely, set:
 * <pre>ftgo.security.jwt.enabled=false</pre>
 */
@Configuration
@ConditionalOnProperty(name = "ftgo.security.jwt.enabled", havingValue = "true", matchIfMissing = true)
@Import(FtgoJwtProperties.class)
public class JwtAutoConfiguration {

    /**
     * Creates the JWT authentication filter bean.
     *
     * <p>This filter is registered in the security filter chain by
     * {@link com.ftgo.security.config.FtgoBaseSecurityConfig}.
     *
     * @param jwtTokenProvider the token provider for validation
     * @return the configured JWT authentication filter
     */
    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider) {
        return new JwtAuthenticationFilter(jwtTokenProvider);
    }
}
