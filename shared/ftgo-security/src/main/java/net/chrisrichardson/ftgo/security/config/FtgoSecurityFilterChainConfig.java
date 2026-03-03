package net.chrisrichardson.ftgo.security.config;

import net.chrisrichardson.ftgo.security.FtgoSecurityProperties;
import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.boot.actuate.info.InfoEndpoint;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;

/**
 * Base Spring Security configuration for FTGO microservices.
 *
 * <p>This configuration provides the following security defaults:</p>
 * <ul>
 *   <li>All REST endpoints require authentication by default</li>
 *   <li>CSRF is disabled for stateless REST APIs</li>
 *   <li>Stateless session management (no HTTP sessions)</li>
 *   <li>Actuator /health and /info endpoints are publicly accessible</li>
 *   <li>All other actuator endpoints require authentication</li>
 *   <li>HTTP Basic authentication enabled as a fallback</li>
 *   <li>CORS configuration applied from {@link FtgoCorsConfig}</li>
 * </ul>
 *
 * <p>Services can override this configuration by extending
 * {@link WebSecurityConfigurerAdapter} with a higher {@code @Order}.</p>
 */
@EnableWebSecurity
public class FtgoSecurityFilterChainConfig extends WebSecurityConfigurerAdapter {

    private final FtgoSecurityProperties securityProperties;

    public FtgoSecurityFilterChainConfig(FtgoSecurityProperties securityProperties) {
        this.securityProperties = securityProperties;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // Disable CSRF for stateless REST APIs
        http.csrf().disable();

        // Configure stateless session management
        http.sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS);

        // Configure CORS
        http.cors();

        // Configure authorization rules
        http.authorizeRequests()
                // Actuator health and info endpoints are public
                .requestMatchers(EndpointRequest.to(HealthEndpoint.class, InfoEndpoint.class)).permitAll()
                // All other actuator endpoints require authentication
                .requestMatchers(EndpointRequest.toAnyEndpoint()).authenticated()
                // Allow CORS preflight requests
                .antMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                // Configure additional public paths from properties
                .antMatchers(securityProperties.getPublicPathsArray()).permitAll()
                // All other requests require authentication
                .anyRequest().authenticated();

        // Enable HTTP Basic authentication as fallback
        http.httpBasic();

        // Configure custom exception handling
        http.exceptionHandling()
                .authenticationEntryPoint(new FtgoAuthenticationEntryPoint())
                .accessDeniedHandler(new FtgoAccessDeniedHandler());
    }
}
