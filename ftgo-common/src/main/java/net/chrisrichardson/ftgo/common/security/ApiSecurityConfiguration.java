package net.chrisrichardson.ftgo.common.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.util.EnumMap;
import java.util.Map;

@Configuration
public class ApiSecurityConfiguration {

  @Bean
  public RoleApiKeyAuthorizer roleApiKeyAuthorizer(Environment environment) {
    Map<ApiRole, String> keys = new EnumMap<>(ApiRole.class);
    for (ApiRole role : ApiRole.values()) {
      keys.put(role, environment.getProperty(role.getPropertyName()));
    }
    return new RoleApiKeyAuthorizer(keys);
  }
}
