package net.chrisrichardson.ftgo.security;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

@Configuration
@EnableWebSecurity
@EnableConfigurationProperties(FtgoSecurityProperties.class)
public class FtgoSecurityConfiguration extends WebSecurityConfigurerAdapter {

  public static final String ROLE_API = "API";
  public static final String ROLE_OPERATOR = "OPERATOR";

  private final FtgoSecurityProperties properties;

  public FtgoSecurityConfiguration(FtgoSecurityProperties properties) {
    this.properties = properties;
  }

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http
            .csrf().disable()
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .authorizeRequests()
            .antMatchers(HttpMethod.GET, "/", "/index.html", "/css/**", "/js/**").permitAll()
            .antMatchers(HttpMethod.GET, "/actuator/health").permitAll()
            .antMatchers("/api/tracking/**").hasRole(ROLE_OPERATOR)
            .anyRequest().hasAnyRole(ROLE_API, ROLE_OPERATOR)
            .and()
            .httpBasic();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  @Override
  public UserDetailsService userDetailsService() {
    PasswordEncoder encoder = passwordEncoder();
    InMemoryUserDetailsManager manager = new InMemoryUserDetailsManager();
    manager.createUser(User.withUsername(properties.getApi().getUsername())
            .password(encoder.encode(properties.getApi().getPassword()))
            .roles(ROLE_API)
            .build());
    manager.createUser(User.withUsername(properties.getOperator().getUsername())
            .password(encoder.encode(properties.getOperator().getPassword()))
            .roles(ROLE_OPERATOR)
            .build());
    return manager;
  }
}
