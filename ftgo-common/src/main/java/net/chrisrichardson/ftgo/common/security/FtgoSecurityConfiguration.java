package net.chrisrichardson.ftgo.common.security;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@EnableConfigurationProperties(FtgoSecurityProperties.class)
public class FtgoSecurityConfiguration {

  @Bean
  public PasswordEncoder passwordEncoder() {
    return PasswordEncoderFactories.createDelegatingPasswordEncoder();
  }

  @Bean
  public UserDetailsService ftgoUserDetailsService(FtgoSecurityProperties properties, PasswordEncoder passwordEncoder) {
    return new FtgoUserDetailsService(properties, passwordEncoder);
  }
}
