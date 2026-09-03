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
import org.springframework.security.core.userdetails.UserDetails;
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
    manager.createUser(user(encoder, "ftgo.security.api", properties.getApi(), ROLE_API));
    manager.createUser(user(encoder, "ftgo.security.operator", properties.getOperator(), ROLE_OPERATOR));
    return manager;
  }

  private static UserDetails user(PasswordEncoder encoder, String prefix, FtgoSecurityProperties.Account account, String role) {
    if (isBlank(account.getUsername()) || isBlank(account.getPassword())) {
      throw new IllegalStateException(prefix + ".username and " + prefix + ".password must be configured");
    }
    return User.withUsername(account.getUsername())
            .password(encoder.encode(account.getPassword()))
            .roles(role)
            .build();
  }

  private static boolean isBlank(String s) {
    return s == null || s.trim().isEmpty();
  }
}
