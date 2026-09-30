package net.chrisrichardson.ftgo.orderservice.web;

import net.chrisrichardson.ftgo.common.security.FtgoSecurityConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;

/**
 * Requires HTTP Basic authentication for every state-changing request under /orders.
 * Per-order ownership is enforced by {@link OrderAccessPolicy} in {@link OrderController}.
 */
@Configuration
@EnableWebSecurity
@Import(FtgoSecurityConfiguration.class)
public class OrderSecurityConfiguration extends WebSecurityConfigurerAdapter {

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http
            .requestMatchers().antMatchers("/orders", "/orders/**")
            .and()
            .csrf().disable()
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .authorizeRequests()
            .antMatchers(HttpMethod.GET, "/orders", "/orders/**").permitAll()
            .anyRequest().authenticated()
            .and()
            .httpBasic();
  }

  @Bean
  public OrderAccessPolicy orderAccessPolicy() {
    return new OrderAccessPolicy();
  }
}
