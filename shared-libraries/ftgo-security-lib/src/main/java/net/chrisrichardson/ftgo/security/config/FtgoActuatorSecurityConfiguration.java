package net.chrisrichardson.ftgo.security.config;

import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.boot.actuate.info.InfoEndpoint;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;

/**
 * Security configuration for Spring Boot Actuator endpoints.
 *
 * <p>Evaluated before the main security configuration ({@link FtgoWebSecurityConfiguration})
 * due to its lower {@link Order} value. This ensures actuator-specific rules are applied first.
 *
 * <p>Policy:
 * <ul>
 *   <li>Health and Info endpoints are publicly accessible (for load balancer probes)</li>
 *   <li>All other actuator endpoints require authentication</li>
 * </ul>
 */
@Configuration
@Order(99)
public class FtgoActuatorSecurityConfiguration extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .requestMatcher(EndpointRequest.toAnyEndpoint())

            .csrf().disable()

            .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()

            .authorizeRequests()
                .requestMatchers(EndpointRequest.to(HealthEndpoint.class, InfoEndpoint.class))
                    .permitAll()
                .anyRequest()
                    .authenticated();
    }
}
