package net.chrisrichardson.ftgo.consumerservice.web;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class ConsumerSecurityConfiguration extends WebSecurityConfigurerAdapter {

  public static final String ADMIN_ROLE = "ADMIN";

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http.antMatcher("/consumers/**")
            .csrf().disable()
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .authorizeRequests()
            .antMatchers(HttpMethod.GET, "/consumers/**").authenticated()
            .anyRequest().permitAll()
            .and()
            .httpBasic();
  }
}
