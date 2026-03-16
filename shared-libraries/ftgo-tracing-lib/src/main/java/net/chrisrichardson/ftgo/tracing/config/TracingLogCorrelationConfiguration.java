package net.chrisrichardson.ftgo.tracing.config;

import brave.Tracer;
import brave.Tracing;
import brave.propagation.TraceContext;
import org.slf4j.MDC;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
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
import java.io.IOException;

/**
 * Configuration for correlating trace context with log output.
 *
 * <p>Registers a servlet filter that injects {@code traceId} and {@code spanId}
 * into the SLF4J MDC so that every log line emitted during a traced request
 * carries tracing identifiers. This is complementary to Sleuth's built-in
 * MDC integration and ensures fields are available even in custom log patterns.</p>
 */
@Configuration
@ConditionalOnProperty(name = "ftgo.tracing.log-correlation.enabled", havingValue = "true", matchIfMissing = true)
@ConditionalOnClass(name = "org.slf4j.MDC")
public class TracingLogCorrelationConfiguration {

    public static final String MDC_TRACE_ID = "traceId";
    public static final String MDC_SPAN_ID = "spanId";
    public static final String MDC_PARENT_SPAN_ID = "parentSpanId";
    public static final String MDC_SAMPLED = "traceSampled";

    @Bean
    public FilterRegistrationBean<TracingMdcFilter> tracingMdcFilterRegistration(Tracer tracer) {
        FilterRegistrationBean<TracingMdcFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new TracingMdcFilter(tracer));
        registration.addUrlPatterns("/*");
        registration.setName("tracingMdcFilter");
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE + 1);
        return registration;
    }

    /**
     * Servlet filter that populates MDC with trace/span IDs from the current Brave span.
     */
    static class TracingMdcFilter implements Filter {

        private final Tracer tracer;

        TracingMdcFilter(Tracer tracer) {
            this.tracer = tracer;
        }

        @Override
        public void init(FilterConfig filterConfig) {
            // no-op
        }

        @Override
        public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
                throws IOException, ServletException {
            brave.Span currentSpan = tracer.currentSpan();
            if (currentSpan != null) {
                TraceContext context = currentSpan.context();
                MDC.put(MDC_TRACE_ID, context.traceIdString());
                MDC.put(MDC_SPAN_ID, Long.toHexString(context.spanId()));
                if (context.parentId() != null) {
                    MDC.put(MDC_PARENT_SPAN_ID, Long.toHexString(context.parentId()));
                }
                MDC.put(MDC_SAMPLED, context.sampled() != null ? context.sampled().toString() : "false");
            }
            try {
                chain.doFilter(request, response);
            } finally {
                MDC.remove(MDC_TRACE_ID);
                MDC.remove(MDC_SPAN_ID);
                MDC.remove(MDC_PARENT_SPAN_ID);
                MDC.remove(MDC_SAMPLED);
            }
        }

        @Override
        public void destroy() {
            // no-op
        }
    }
}
