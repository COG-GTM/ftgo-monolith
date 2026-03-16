package net.chrisrichardson.ftgo.logging;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.core.Ordered;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

/**
 * Auto-configuration for the FTGO centralized logging library.
 * Registers the correlation ID servlet filter, logging aspect,
 * and provides a RestTemplate bean pre-configured with correlation ID propagation.
 */
@Configuration
@EnableAspectJAutoProxy
public class LoggingAutoConfiguration {

    @Value("${spring.application.name:ftgo-application}")
    private String serviceName;

    @Bean
    public CorrelationIdFilter correlationIdFilter() {
        return new CorrelationIdFilter(serviceName);
    }

    @Bean
    public FilterRegistrationBean<CorrelationIdFilter> correlationIdFilterRegistration(
            CorrelationIdFilter correlationIdFilter) {
        FilterRegistrationBean<CorrelationIdFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(correlationIdFilter);
        registration.addUrlPatterns("/*");
        registration.setName("correlationIdFilter");
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return registration;
    }

    @Bean
    public CorrelationIdInterceptor correlationIdInterceptor() {
        return new CorrelationIdInterceptor();
    }

    @Bean
    public LoggingAspect loggingAspect() {
        return new LoggingAspect();
    }

    /**
     * Provides a RestTemplate with correlation ID propagation.
     * Services can inject this bean to automatically propagate
     * correlation IDs in inter-service HTTP calls.
     */
    @Bean
    public RestTemplate loggingRestTemplate(CorrelationIdInterceptor correlationIdInterceptor) {
        RestTemplate restTemplate = new RestTemplate();
        List<ClientHttpRequestInterceptor> interceptors = new ArrayList<>(restTemplate.getInterceptors());
        interceptors.add(correlationIdInterceptor);
        restTemplate.setInterceptors(interceptors);
        return restTemplate;
    }
}
