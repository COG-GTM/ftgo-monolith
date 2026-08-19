package net.chrisrichardson.ftgo.courierservice.web.security;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Courier endpoints expose personal data (name, address and live GPS coordinates), so they
 * require an authenticated dispatcher or the courier's own account. Accounts are configured
 * with the ftgo.courier.security.users.* properties; when none are configured nobody can
 * authenticate and the endpoints are closed.
 */
@Configuration
@EnableWebSecurity
@EnableConfigurationProperties(CourierSecurityProperties.class)
public class CourierSecurityConfiguration extends WebSecurityConfigurerAdapter {

  private final CourierSecurityProperties properties;

  public CourierSecurityConfiguration(CourierSecurityProperties properties) {
    this.properties = properties;
  }

  @Bean
  public CourierAccessPolicy courierAccessPolicy() {
    return new CourierAccessPolicy();
  }

  @Bean
  public PasswordEncoder courierPasswordEncoder() {
    return PasswordEncoderFactories.createDelegatingPasswordEncoder();
  }

  @Bean
  public UserDetailsService courierUserDetailsService() {
    Map<String, UserDetails> usersByUsername = new HashMap<>();
    for (CourierSecurityProperties.User user : properties.getUsers()) {
      usersByUsername.put(user.getUsername(), toUserDetails(user));
    }
    return username -> {
      UserDetails userDetails = usersByUsername.get(username);
      if (userDetails == null)
        throw new UsernameNotFoundException("No such user");
      return userDetails;
    };
  }

  private UserDetails toUserDetails(CourierSecurityProperties.User user) {
    if (user.getUsername() == null || user.getUsername().isEmpty()
            || user.getPassword() == null || user.getPassword().isEmpty()
            || user.getRole() == null)
      throw new IllegalStateException("ftgo.courier.security.users entries require a username, password and role");
    if (user.getRole() == CourierSecurityProperties.Role.COURIER && user.getCourierId() == null)
      throw new IllegalStateException("A COURIER account requires a courierId: " + user.getUsername());
    return new CourierUserDetails(user.getUsername(), user.getPassword(), user.getCourierId(),
            Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole().name())));
  }

  @Override
  protected void configure(AuthenticationManagerBuilder auth) throws Exception {
    auth.userDetailsService(courierUserDetailsService()).passwordEncoder(courierPasswordEncoder());
  }

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http
            .csrf().disable()
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .authorizeRequests()
            .antMatchers(HttpMethod.POST, "/couriers").hasRole(CourierSecurityProperties.Role.DISPATCHER.name())
            .antMatchers("/couriers", "/couriers/**").authenticated()
            .anyRequest().permitAll()
            .and()
            .httpBasic();
  }
}
