package net.chrisrichardson.ftgo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.util.StringUtils;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

  private static final Logger logger = LoggerFactory.getLogger(SecurityConfiguration.class);

  @Value("${ftgo.security.operator.username:operator}")
  private String username;

  @Value("${ftgo.security.operator.password:}")
  private String password;

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http.csrf().disable()
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
            .httpBasic().and()
            .authorizeRequests()
            .antMatchers(HttpMethod.GET, "/couriers/**").hasRole("OPERATOR")
            .anyRequest().permitAll();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return PasswordEncoderFactories.createDelegatingPasswordEncoder();
  }

  @Bean
  public UserDetailsService userDetailsService(PasswordEncoder passwordEncoder) {
    if (StringUtils.hasText(password)) {
      return new InMemoryUserDetailsManager(User.withUsername(username)
              .password(passwordEncoder.encode(password))
              .roles("OPERATOR")
              .build());
    }
    logger.warn("Courier read endpoints are inaccessible until FTGO_OPERATOR_PASSWORD is set");
    return new InMemoryUserDetailsManager();
  }
}
