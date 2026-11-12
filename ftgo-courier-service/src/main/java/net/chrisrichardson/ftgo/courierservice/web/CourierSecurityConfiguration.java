package net.chrisrichardson.ftgo.courierservice.web;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.util.matcher.AndRequestMatcher;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.NegatedRequestMatcher;

@Configuration
@EnableWebSecurity
public class CourierSecurityConfiguration extends WebSecurityConfigurerAdapter {

  @Autowired
  private FtgoUserDetailsService userDetailsService;

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Override
  protected void configure(AuthenticationManagerBuilder auth) throws Exception {
    auth.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder());
  }

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http.requestMatcher(new AndRequestMatcher(
            new AntPathRequestMatcher("/couriers/**"),
            new NegatedRequestMatcher(new AntPathRequestMatcher("/couriers/**", "POST"))))
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .csrf().disable()
            .httpBasic()
            .and()
            .authorizeRequests()
            .mvcMatchers(HttpMethod.PUT, "/couriers/{courierId}/credentials").hasRole("OPERATOR")
            .antMatchers(HttpMethod.PUT, "/couriers/*/credentials.*").hasRole("OPERATOR")
            .anyRequest().authenticated();
  }
}
