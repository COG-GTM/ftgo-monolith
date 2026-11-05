package net.chrisrichardson.ftgo.common.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
public class ConsumerAuthenticationConfiguration implements WebMvcConfigurer {

  @Autowired
  private ConsumerTokenService consumerTokenService;

  @Bean
  public ConsumerTokenService consumerTokenService(@Value("${ftgo.security.consumer-token-secret:}") String secret) {
    return new ConsumerTokenService(secret);
  }

  @Override
  public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
    resolvers.add(new ConsumerAuthenticationArgumentResolver(consumerTokenService));
  }
}
