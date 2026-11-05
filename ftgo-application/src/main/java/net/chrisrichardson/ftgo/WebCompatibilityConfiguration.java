package net.chrisrichardson.ftgo;

import jakarta.servlet.Filter;
import jakarta.servlet.http.HttpServletRequest;
import net.chrisrichardson.ftgo.common.tracking.OriginalRequestUri;
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
   * Records the client's request URI before {@link #trailingSlashUrlHandlerFilter()} rewrites it, so API
   * tracking logs and error bodies keep reporting e.g. "/orders/".
   */
  @Bean
  public FilterRegistrationBean<Filter> originalRequestUriFilter() {
    FilterRegistrationBean<Filter> registration = new FilterRegistrationBean<>((request, response, chain) -> {
      if (request.getAttribute(OriginalRequestUri.ATTRIBUTE) == null && request instanceof HttpServletRequest http) {
        request.setAttribute(OriginalRequestUri.ATTRIBUTE, http.getRequestURI());
      }
      chain.doFilter(request, response);
    });
    registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
    return registration;
  }

  /**
   * Spring Framework 6 no longer matches "/orders/" to "/orders": strip the trailing slash before
   * dispatch (AB-561 decision Q6).
   */
  @Bean
  public FilterRegistrationBean<UrlHandlerFilter> trailingSlashUrlHandlerFilter() {
    FilterRegistrationBean<UrlHandlerFilter> registration =
            new FilterRegistrationBean<>(UrlHandlerFilter.trailingSlashHandler("/**").wrapRequest().build());
    registration.setOrder(Ordered.HIGHEST_PRECEDENCE + 1);
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
