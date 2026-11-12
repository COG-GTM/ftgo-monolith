package net.chrisrichardson.ftgo.security;

import java.util.Map;
import java.util.LinkedHashMap;
import java.util.UUID;

import net.chrisrichardson.ftgo.common.security.FtgoRole;
import net.chrisrichardson.ftgo.common.security.FtgoUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;

@Configuration
@EnableWebSecurity
@EnableConfigurationProperties(FtgoSecurityProperties.class)
public class FtgoSecurityConfiguration extends WebSecurityConfigurerAdapter {

  private static final Logger logger = LoggerFactory.getLogger(FtgoSecurityConfiguration.class);

  @Autowired
  @Qualifier("ftgoUserDetailsService")
  private UserDetailsService ftgoUserDetailsService;

  @Autowired
  private PasswordEncoder passwordEncoder;

  @Bean
  public PasswordEncoder passwordEncoder() {
    return PasswordEncoderFactories.createDelegatingPasswordEncoder();
  }

  @Bean
  public UserDetailsService ftgoUserDetailsService(FtgoSecurityProperties properties, PasswordEncoder passwordEncoder) {
    Map<String, FtgoUser> users = new LinkedHashMap<>();
    String adminUsername = properties.getAdmin().getUsername();
    String adminPassword = properties.getAdmin().getPassword();
    if (adminPassword == null || adminPassword.trim().isEmpty()) {
      adminPassword = UUID.randomUUID().toString();
      logger.warn("Using generated FTGO admin password: {} (set FTGO_ADMIN_PASSWORD to override)", adminPassword);
    }
    users.put(adminUsername, new FtgoUser(adminUsername, passwordEncoder.encode(adminPassword), FtgoRole.ADMIN, null));

    for (Map.Entry<String, FtgoSecurityProperties.UserProperties> entry : properties.getUsers().entrySet()) {
      String username = entry.getKey();
      FtgoSecurityProperties.UserProperties userProperties = entry.getValue();
      if (adminUsername.equals(username)) {
        throw new IllegalStateException("User username collides with admin username: " + username);
      }

      int configuredSubjects = (userProperties.getConsumerId() == null ? 0 : 1)
              + (userProperties.getRestaurantId() == null ? 0 : 1)
              + (userProperties.getCourierId() == null ? 0 : 1);
      if (configuredSubjects != 1) {
        throw new IllegalStateException("User must configure exactly one subject ID: " + username);
      }

      FtgoRole role;
      Long subjectId;
      if (userProperties.getConsumerId() != null) {
        role = FtgoRole.CONSUMER;
        subjectId = userProperties.getConsumerId();
      } else if (userProperties.getRestaurantId() != null) {
        role = FtgoRole.RESTAURANT;
        subjectId = userProperties.getRestaurantId();
      } else {
        role = FtgoRole.COURIER;
        subjectId = userProperties.getCourierId();
      }
      users.put(username, new FtgoUser(username, userProperties.getPassword(), role, subjectId));
    }
    return username -> {
      FtgoUser user = users.get(username);
      if (user == null) {
        throw new UsernameNotFoundException("User not found: " + username);
      }
      return new FtgoUser(user.getUsername(), user.getPassword(), user.getRole(), user.getSubjectId());
    };
  }

  @Override
  protected void configure(AuthenticationManagerBuilder auth) throws Exception {
    auth.userDetailsService(ftgoUserDetailsService).passwordEncoder(passwordEncoder);
  }

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    AuthenticationEntryPoint entryPoint = new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED);
    http.csrf().disable() // Credentials are only accepted from an explicitly set Authorization header, sessions are stateless and the 401 entry point sends no Basic challenge, so browsers never cache/auto-attach credentials and cross-site forms cannot forge requests.
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .httpBasic().authenticationEntryPoint(entryPoint)
            .and()
            .exceptionHandling().authenticationEntryPoint(entryPoint)
            .and()
            .authorizeRequests()
            .antMatchers(HttpMethod.GET, "/", "/index.html", "/css/**", "/js/**", "/favicon.ico",
                    "/config/sentry", "/actuator/health", "/actuator/info", "/swagger-ui.html",
                    "/webjars/**", "/swagger-resources/**", "/v2/api-docs").permitAll()
            .antMatchers("/actuator/**").hasRole("ADMIN")
            .anyRequest().authenticated();
  }
}
