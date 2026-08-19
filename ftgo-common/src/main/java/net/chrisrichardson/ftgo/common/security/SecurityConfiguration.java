package net.chrisrichardson.ftgo.common.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

  private final AppUserRepository appUserRepository;

  public SecurityConfiguration(AppUserRepository appUserRepository) {
    this.appUserRepository = appUserRepository;
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public FtgoUserDetailsService ftgoUserDetailsService() {
    return new FtgoUserDetailsService(appUserRepository);
  }

  @Override
  protected void configure(AuthenticationManagerBuilder auth) throws Exception {
    auth.userDetailsService(ftgoUserDetailsService()).passwordEncoder(passwordEncoder());
  }

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http
            // The API is stateless and authenticated per request with HTTP Basic,
            // so there is no session or cookie for CSRF to protect.
            .csrf().disable()
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .authorizeRequests()
            .antMatchers(HttpMethod.GET, "/", "/index.html", "/favicon.ico", "/css/**", "/js/**").permitAll()
            .antMatchers(HttpMethod.GET, "/actuator/health", "/actuator/info").permitAll()
            .antMatchers(HttpMethod.GET, "/swagger-ui.html", "/swagger-resources/**", "/v2/api-docs", "/webjars/**").permitAll()
            .anyRequest().authenticated()
            .and()
            .httpBasic();
  }
}
