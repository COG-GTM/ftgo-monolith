package net.chrisrichardson.ftgo;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.error.DefaultErrorAttributes;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.filter.UrlHandlerFilter;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * Keeps the pre-Spring 6 web contract used by the dashboard and the e2e tests.
 */
@Configuration
public class WebCompatibilityConfiguration {

  /**
   * Spring Framework 6 no longer matches "/orders/" to "/orders": strip the trailing slash before
   * dispatch (AB-561 decision Q6).
   */
  @Bean
  public FilterRegistrationBean<UrlHandlerFilter> trailingSlashUrlHandlerFilter() {
    FilterRegistrationBean<UrlHandlerFilter> registration =
            new FilterRegistrationBean<>(UrlHandlerFilter.trailingSlashHandler("/**").wrapRequest().build());
    registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
    return registration;
  }

  /**
   * Unknown static paths used to reach /error without an exception; keep their "No message available" body.
   */
  @Bean
  public ErrorAttributes errorAttributes() {
    return new DefaultErrorAttributes() {
      @Override
      protected String getMessage(WebRequest webRequest, Throwable error) {
        return super.getMessage(webRequest, error instanceof NoResourceFoundException ? null : error);
      }
    };
  }
}
