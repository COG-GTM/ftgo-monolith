package net.chrisrichardson.ftgo.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@EnableWebSecurity
@EnableConfigurationProperties(FtgoSecurityProperties.class)
public class FtgoSecurityConfiguration extends WebSecurityConfigurerAdapter {

  @Autowired
  private FtgoUserDetailsService userDetailsService;

  @Autowired
  private PasswordEncoder passwordEncoder;

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public DaoAuthenticationProvider daoAuthenticationProvider() {
    DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
    provider.setUserDetailsService(userDetailsService);
    provider.setPasswordEncoder(passwordEncoder);
    return provider;
  }

  @Override
  protected void configure(AuthenticationManagerBuilder auth) throws Exception {
    auth.authenticationProvider(daoAuthenticationProvider());
  }

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http.csrf().disable()
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
            .httpBasic().realmName("ftgo").and()
            .authorizeRequests()
            .mvcMatchers(HttpMethod.GET, "/consumers/{consumerId}")
            .access("hasRole('OPERATOR') or @consumerAccess.isSelf(authentication, #consumerId)")
            .mvcMatchers("/consumers", "/consumers/", "/consumers/**").hasRole("OPERATOR")
            .mvcMatchers(HttpMethod.POST, "/restaurants", "/restaurants/").hasAnyRole("OPERATOR", "RESTAURANT_OWNER")
            .anyRequest().permitAll();
  }
}
