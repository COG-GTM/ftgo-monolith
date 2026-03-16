package net.chrisrichardson.ftgo.metrics.config;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Timer;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;

/**
 * Registers an HTTP request metrics filter that records RED metrics
 * (Rate, Errors, Duration) for all incoming HTTP requests.
 */
@Configuration
public class MetricsWebFilterConfiguration {

    @Bean
    public FilterRegistrationBean<HttpMetricsFilter> httpMetricsFilterRegistration(MeterRegistry meterRegistry) {
        FilterRegistrationBean<HttpMetricsFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new HttpMetricsFilter(meterRegistry));
        registration.addUrlPatterns("/*");
        registration.setName("httpMetricsFilter");
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE + 1);
        return registration;
    }

    /**
     * Servlet filter that records HTTP request metrics using Micrometer.
     * Captures request count, error rate, and duration for RED dashboards.
     */
    static class HttpMetricsFilter implements Filter {

        private final MeterRegistry meterRegistry;

        HttpMetricsFilter(MeterRegistry meterRegistry) {
            this.meterRegistry = meterRegistry;
        }

        @Override
        public void init(FilterConfig filterConfig) throws ServletException {
            // no-op
        }

        @Override
        public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
                throws IOException, ServletException {
            HttpServletRequest httpRequest = (HttpServletRequest) request;
            HttpServletResponse httpResponse = (HttpServletResponse) response;

            Timer.Sample sample = Timer.start(meterRegistry);

            try {
                chain.doFilter(request, response);
            } finally {
                String method = httpRequest.getMethod();
                String uri = normalizeUri(httpRequest.getRequestURI());
                int status = httpResponse.getStatus();
                String outcome = status >= 500 ? "SERVER_ERROR"
                        : status >= 400 ? "CLIENT_ERROR"
                        : "SUCCESS";

                sample.stop(Timer.builder("http_server_requests")
                        .tags(Arrays.asList(
                                Tag.of("method", method),
                                Tag.of("uri", uri),
                                Tag.of("status", String.valueOf(status)),
                                Tag.of("outcome", outcome)
                        ))
                        .description("HTTP request duration")
                        .register(meterRegistry));

                meterRegistry.counter("http_server_requests_total",
                        "method", method,
                        "uri", uri,
                        "status", String.valueOf(status),
                        "outcome", outcome
                ).increment();
            }
        }

        @Override
        public void destroy() {
            // no-op
        }

        /**
         * Normalizes URI paths to reduce cardinality by replacing
         * numeric path segments with placeholders.
         */
        private String normalizeUri(String uri) {
            if (uri == null || uri.isEmpty()) {
                return "/";
            }
            // Replace numeric path segments (e.g., /orders/123 -> /orders/{id})
            return uri.replaceAll("/\\d+", "/{id}");
        }
    }
}
