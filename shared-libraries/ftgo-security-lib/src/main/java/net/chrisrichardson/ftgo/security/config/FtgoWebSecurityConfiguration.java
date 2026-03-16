package net.chrisrichardson.ftgo.security.config;

import net.chrisrichardson.ftgo.security.handler.FtgoAccessDeniedHandler;
import net.chrisrichardson.ftgo.security.handler.FtgoAuthenticationEntryPoint;
import net.chrisrichardson.ftgo.security.jwt.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Base Spring Security configuration for FTGO microservices.
 *
 * <p>Provides a stateless security configuration suitable for REST APIs:
 * <ul>
 *   <li>CSRF disabled (stateless, token-based auth)</li>
 *   <li>Stateless session management (no HTTP session)</li>
 *   <li>Public access to health and info actuator endpoints</li>
 *   <li>All other requests require authentication</li>
 *   <li>Custom exception handlers for 401/403 responses</li>
 * </ul>
 *
 * <p>Services can override this configuration by providing their own
 * {@link WebSecurityConfigurerAdapter} with a higher {@link Order} value.
 */
@Configuration
@EnableWebSecurity
@Order(100)
public class FtgoWebSecurityConfiguration extends WebSecurityConfigurerAdapter {

    @Value("${ftgo.security.public-paths:/actuator/health,/actuator/info}")
    private String[] publicPaths;

    @Autowired(required = false)
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .csrf().disable()

            .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()

            .exceptionHandling()
                .authenticationEntryPoint(ftgoAuthenticationEntryPoint())
                .accessDeniedHandler(ftgoAccessDeniedHandler())
            .and()

            .authorizeRequests()
                .antMatchers(publicPaths).permitAll()
                .antMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .anyRequest().authenticated();

        if (jwtAuthenticationFilter != null) {
            http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        }
    }

    @Bean
    public FtgoAuthenticationEntryPoint ftgoAuthenticationEntryPoint() {
        return new FtgoAuthenticationEntryPoint();
    }

    @Bean
    public FtgoAccessDeniedHandler ftgoAccessDeniedHandler() {
        return new FtgoAccessDeniedHandler();
    }
}
