package net.chrisrichardson.ftgo.jwt.config;

import net.chrisrichardson.ftgo.jwt.FtgoJwtProperties;
import net.chrisrichardson.ftgo.jwt.filter.JwtTokenAuthenticationFilter;
import net.chrisrichardson.ftgo.jwt.service.JwtTokenService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security configuration that integrates JWT authentication into the
 * security filter chain.
 *
 * <p>This configuration:</p>
 * <ul>
 *   <li>Registers the {@link JwtTokenAuthenticationFilter} before the
 *       UsernamePasswordAuthenticationFilter in the filter chain</li>
 *   <li>Configures stateless session management (no HTTP sessions)</li>
 *   <li>Disables CSRF for stateless REST APIs</li>
 *   <li>Permits access to excluded paths defined in {@link FtgoJwtProperties}</li>
 *   <li>Requires authentication for all other requests</li>
 * </ul>
 *
 * <p>This configuration is only active when {@code ftgo.jwt.enabled=true} (default).
 * It can be disabled by setting {@code ftgo.jwt.enabled=false} in
 * application.yml.</p>
 *
 * @see JwtTokenAuthenticationFilter
 * @see FtgoJwtProperties
 */
@Configuration
@EnableWebSecurity
@ConditionalOnProperty(prefix = "ftgo.jwt", name = "enabled", havingValue = "true", matchIfMissing = true)
public class FtgoJwtSecurityConfig extends WebSecurityConfigurerAdapter {

    private final JwtTokenService jwtTokenService;
    private final FtgoJwtProperties jwtProperties;

    public FtgoJwtSecurityConfig(JwtTokenService jwtTokenService,
                                  FtgoJwtProperties jwtProperties) {
        this.jwtTokenService = jwtTokenService;
        this.jwtProperties = jwtProperties;
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

        // Return 401 instead of 403 for unauthenticated requests
        http.exceptionHandling()
                .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED));

        // Configure authorization rules
        http.authorizeRequests()
                // Allow excluded paths without authentication
                .antMatchers(jwtProperties.getExcludedPathsArray()).permitAll()
                // All other requests require authentication
                .anyRequest().authenticated();

        // Register JWT filter before UsernamePasswordAuthenticationFilter
        http.addFilterBefore(
                jwtTokenAuthenticationFilter(),
                UsernamePasswordAuthenticationFilter.class);
    }

    @Bean
    public JwtTokenAuthenticationFilter jwtTokenAuthenticationFilter() {
        return new JwtTokenAuthenticationFilter(jwtTokenService, jwtProperties);
    }
}
