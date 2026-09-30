package net.chrisrichardson.ftgo.orderservice.security;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@EnableWebSecurity
@EnableConfigurationProperties(FtgoSecurityProperties.class)
public class OrderSecurityConfiguration extends WebSecurityConfigurerAdapter {

  private final FtgoSecurityProperties securityProperties;

  public OrderSecurityConfiguration(FtgoSecurityProperties securityProperties) {
    this.securityProperties = securityProperties;
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return PasswordEncoderFactories.createDelegatingPasswordEncoder();
  }

  @Bean
  @Override
  public UserDetailsService userDetailsService() {
    return new PropertiesUserDetailsService(securityProperties, passwordEncoder());
  }

  @Bean
  public OrderAccessPolicy orderAccessPolicy() {
    return new OrderAccessPolicy();
  }

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http
            .csrf().disable()
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .httpBasic()
            .and()
            .authorizeRequests()
            .antMatchers(HttpMethod.POST,
                    "/orders/*/accept", "/orders/*/preparing", "/orders/*/ready",
                    "/orders/*/pickedup", "/orders/*/delivered").hasRole(FtgoRoles.OPERATIONS)
            .antMatchers("/orders", "/orders/**").authenticated()
            .anyRequest().permitAll();
  }
}
