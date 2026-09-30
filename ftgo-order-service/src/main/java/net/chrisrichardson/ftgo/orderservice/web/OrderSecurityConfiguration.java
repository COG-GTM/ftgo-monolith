package net.chrisrichardson.ftgo.orderservice.web;

import net.chrisrichardson.ftgo.domain.ConsumerRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@EnableWebSecurity
public class OrderSecurityConfiguration extends WebSecurityConfigurerAdapter {

  private final PasswordEncoder passwordEncoder;
  private final ConsumerRepository consumerRepository;

  public OrderSecurityConfiguration(PasswordEncoder passwordEncoder, ConsumerRepository consumerRepository) {
    this.passwordEncoder = passwordEncoder;
    this.consumerRepository = consumerRepository;
  }

  @Bean
  public UserDetailsService consumerUserDetailsService() {
    return new ConsumerUserDetailsService(consumerRepository);
  }

  @Override
  protected void configure(AuthenticationManagerBuilder auth) throws Exception {
    auth.userDetailsService(consumerUserDetailsService()).passwordEncoder(passwordEncoder);
  }

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http.csrf().disable()
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .authorizeRequests()
            .mvcMatchers(HttpMethod.GET, "/orders", "/orders/{orderId}").hasAuthority(ConsumerUserDetails.ROLE_CONSUMER)
            .anyRequest().permitAll()
            .and()
            .httpBasic();
  }
}
