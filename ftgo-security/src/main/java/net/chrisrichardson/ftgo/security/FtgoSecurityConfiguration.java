package net.chrisrichardson.ftgo.security;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.context.annotation.Bean;
import org.springframework.beans.factory.annotation.Autowired;

@Configuration
@EnableWebSecurity
@EntityScan(basePackages = "net.chrisrichardson.ftgo.security")
@EnableJpaRepositories(basePackages = "net.chrisrichardson.ftgo.security")
public class FtgoSecurityConfiguration extends WebSecurityConfigurerAdapter {

  @Autowired
  private FtgoUserDetailsService userDetailsService;

  @Bean
  public PasswordEncoder passwordEncoder() {
    return PasswordEncoderFactories.createDelegatingPasswordEncoder();
  }

  @Override
  protected void configure(AuthenticationManagerBuilder auth) throws Exception {
    auth.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder());
  }

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http.csrf().disable()
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .httpBasic().realmName("ftgo")
            .and()
            .authorizeRequests()
            .antMatchers(HttpMethod.POST, "/couriers").hasRole("OPERATOR")
            .antMatchers("/accounts", "/accounts/**").hasRole("OPERATOR")
            .antMatchers("/orders", "/orders/**", "/couriers/**").authenticated()
            .anyRequest().permitAll();
  }
}
