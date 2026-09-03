package net.chrisrichardson.ftgo.consumerservice.web;

import org.junit.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class OperatorApiKeyAuthorizerWiringTest {

  @Configuration
  static class Config {
    @Bean
    static PropertySourcesPlaceholderConfigurer placeholders() {
      return new PropertySourcesPlaceholderConfigurer();
    }
  }

  private OperatorApiKeyAuthorizer authorizerWithEnv(String... envPairs) {
    AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();
    for (int i = 0; i < envPairs.length; i += 2) {
      System.setProperty(envPairs[i], envPairs[i + 1]);
    }
    try {
      ctx.register(Config.class, OperatorApiKeyAuthorizer.class);
      ctx.refresh();
      return ctx.getBean(OperatorApiKeyAuthorizer.class);
    } finally {
      for (int i = 0; i < envPairs.length; i += 2) {
        System.clearProperty(envPairs[i]);
      }
    }
  }

  private static MockHttpServletRequest bearer(String token) {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.addHeader("Authorization", "Bearer " + token);
    return request;
  }

  @Test
  public void shouldResolveKeyFromProperty() {
    OperatorApiKeyAuthorizer authorizer = authorizerWithEnv("ftgo.security.operator-api-key", "wired-secret");
    assertTrue(authorizer.isOperator(bearer("wired-secret")));
    assertFalse(authorizer.isOperator(bearer("other")));
  }

  @Test
  public void shouldFailClosedWhenPropertyAbsent() {
    OperatorApiKeyAuthorizer authorizer = authorizerWithEnv();
    assertFalse(authorizer.isOperator(bearer("")));
    assertFalse(authorizer.isOperator(new MockHttpServletRequest()));
  }
}
