package net.chrisrichardson.ftgo.courierservice.web;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Requires HTTP Basic authentication for the courier API; per-courier authorization is enforced
 * with {@code @PreAuthorize} on {@link CourierController}.
 */
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
@EnableConfigurationProperties(CourierSecurityProperties.class)
public class CourierSecurityConfiguration extends WebSecurityConfigurerAdapter {

  private final CourierSecurityProperties properties;

  public CourierSecurityConfiguration(CourierSecurityProperties properties) {
    this.properties = properties;
  }

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http
            .requestMatchers().antMatchers("/couriers", "/couriers/**").and()
            .authorizeRequests().anyRequest().authenticated().and()
            .httpBasic().and()
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
            .csrf().disable();
  }

  @Override
  protected void configure(AuthenticationManagerBuilder auth) throws Exception {
    auth.userDetailsService(courierUserDetailsService()).passwordEncoder(passwordEncoder());
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return PasswordEncoderFactories.createDelegatingPasswordEncoder();
  }

  @Bean
  public UserDetailsService courierUserDetailsService() {
    Map<String, CourierSecurityProperties.Account> accounts = new HashMap<>();
    for (CourierSecurityProperties.Account account : properties.getUsers()) {
      if (!StringUtils.hasText(account.getUsername()) && !StringUtils.hasText(account.getPassword()))
        continue;
      validate(account);
      accounts.put(account.getUsername(), account);
    }
    return username -> {
      CourierSecurityProperties.Account account = accounts.get(username);
      if (account == null)
        throw new UsernameNotFoundException(username);
      return new CourierPrincipal(account.getUsername(), account.getPassword(),
              AuthorityUtils.createAuthorityList(account.getRoles().stream().map(r -> "ROLE_" + r).toArray(String[]::new)),
              account.getCourierId());
    };
  }

  private static void validate(CourierSecurityProperties.Account account) {
    if (!StringUtils.hasText(account.getUsername()) || !StringUtils.hasText(account.getPassword()))
      throw new IllegalStateException("ftgo.security.users entries require a username and password");
    if (account.getRoles().contains(CourierAccess.COURIER) && account.getCourierId() == null)
      throw new IllegalStateException("Courier account " + account.getUsername() + " requires a courier-id");
  }
}
