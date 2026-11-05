package net.chrisrichardson.ftgo.common.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.security.SecureRandom;
import java.util.List;

@Configuration
public class AuthenticationConfiguration implements WebMvcConfigurer {

  private static final Logger logger = LoggerFactory.getLogger(AuthenticationConfiguration.class);

  @Autowired
  private AuthTokenService authTokenService;

  @Bean
  public AuthTokenService authTokenService(@Value("${ftgo.auth.token-secret:}") String secret) {
    if (secret == null || secret.trim().isEmpty()) {
      logger.warn("ftgo.auth.token-secret not set; using random per-process key, externally issued tokens will be rejected");
      byte[] randomSecret = new byte[32];
      new SecureRandom().nextBytes(randomSecret);
      return new AuthTokenService(randomSecret);
    }
    return AuthTokenService.fromSecret(secret);
  }

  @Override
  public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
    resolvers.add(new AuthenticatedPrincipalArgumentResolver(authTokenService));
  }
}
