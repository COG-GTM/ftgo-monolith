package net.chrisrichardson.ftgo.common.tracking;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;

/**
 * Restricts the API tracking endpoints to authenticated operators.
 * Only requests under {@value #TRACKING_PATH_PATTERN} pass through this filter chain;
 * the rest of the application is unaffected.
 */
@Configuration
@EnableWebSecurity
public class ApiTrackingSecurityConfiguration extends WebSecurityConfigurerAdapter {

  public static final String TRACKING_PATH_PATTERN = "/api/tracking/**";
  public static final String OPERATOR_ROLE = "OPERATOR";

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http
            .antMatcher(TRACKING_PATH_PATTERN)
            .authorizeRequests()
              .anyRequest().hasRole(OPERATOR_ROLE)
              .and()
            .httpBasic()
              .and()
            .csrf().disable()
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
  }
}
