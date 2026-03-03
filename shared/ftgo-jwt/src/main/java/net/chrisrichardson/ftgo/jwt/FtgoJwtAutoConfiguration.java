package net.chrisrichardson.ftgo.jwt;

import net.chrisrichardson.ftgo.jwt.config.FtgoJwtSecurityConfig;
import net.chrisrichardson.ftgo.jwt.service.JwtTokenService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Auto-configuration for FTGO JWT authentication.
 *
 * <p>This is the main entry point for the FTGO JWT library. When included
 * as a dependency, it automatically configures:</p>
 * <ul>
 *   <li>{@link JwtTokenService} - JWT token encoding, decoding, validation, and refresh</li>
 *   <li>{@link net.chrisrichardson.ftgo.jwt.filter.JwtTokenAuthenticationFilter} -
 *       Servlet filter for token extraction and security context population</li>
 *   <li>{@link FtgoJwtSecurityConfig} - Spring Security filter chain integration</li>
 *   <li>{@link FtgoJwtProperties} - Externalized configuration under {@code ftgo.jwt.*}</li>
 * </ul>
 *
 * <p>To use this library:</p>
 * <ol>
 *   <li>Add {@code ftgo-jwt} as a dependency</li>
 *   <li>Configure {@code ftgo.jwt.secret} (via environment variable recommended)</li>
 *   <li>Optionally configure issuer, expiration, excluded paths, etc.</li>
 * </ol>
 *
 * <p>This configuration is only active for servlet-based web applications.</p>
 *
 * @see FtgoJwtProperties
 * @see JwtTokenService
 * @see FtgoJwtSecurityConfig
 */
@Configuration
@ConditionalOnWebApplication
@EnableConfigurationProperties(FtgoJwtProperties.class)
@Import({
        FtgoJwtSecurityConfig.class
})
public class FtgoJwtAutoConfiguration {

    /**
     * Creates the JWT token service bean.
     *
     * <p>The token service is the core component responsible for all JWT
     * operations. It is injected into the authentication filter and can
     * also be used directly by services that need to generate tokens
     * (e.g., an auth service).</p>
     *
     * @param jwtProperties the JWT configuration properties
     * @return the configured JWT token service
     */
    @Bean
    public JwtTokenService jwtTokenService(FtgoJwtProperties jwtProperties) {
        return new JwtTokenService(jwtProperties);
    }
}
