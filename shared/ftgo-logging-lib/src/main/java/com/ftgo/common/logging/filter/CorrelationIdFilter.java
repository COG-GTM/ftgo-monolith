package com.ftgo.common.logging.filter;

import com.ftgo.common.logging.config.LoggingProperties;
import org.slf4j.MDC;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;

/**
 * Servlet filter that extracts or generates a correlation ID for each request.
 *
 * <p>The correlation ID is extracted from the HTTP request header
 * (default: {@code X-Correlation-ID}) set by the API Gateway. If no header
 * is present, a new UUID is generated (when {@code generateIfMissing} is true).</p>
 *
 * <p>The correlation ID is placed in the MDC so it appears in all log entries
 * during request processing, enabling cross-service log correlation.</p>
 *
 * <p>The correlation ID is also added to the HTTP response header for
 * downstream tracing.</p>
 *
 * @see LoggingProperties.CorrelationId
 */
public class CorrelationIdFilter implements Filter {

    private final String headerName;
    private final String mdcKey;
    private final boolean generateIfMissing;

    public CorrelationIdFilter(LoggingProperties properties) {
        this.headerName = properties.getCorrelationId().getHeaderName();
        this.mdcKey = properties.getCorrelationId().getMdcKey();
        this.generateIfMissing = properties.getCorrelationId().isGenerateIfMissing();
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // No initialization needed
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        try {
            String correlationId = extractOrGenerateCorrelationId(request);

            if (correlationId != null) {
                MDC.put(mdcKey, correlationId);

                // Add correlation ID to response header
                if (response instanceof HttpServletResponse) {
                    ((HttpServletResponse) response).setHeader(headerName, correlationId);
                }
            }

            chain.doFilter(request, response);
        } finally {
            MDC.remove(mdcKey);
        }
    }

    @Override
    public void destroy() {
        // No cleanup needed
    }

    private String extractOrGenerateCorrelationId(ServletRequest request) {
        if (request instanceof HttpServletRequest) {
            HttpServletRequest httpRequest = (HttpServletRequest) request;
            String correlationId = httpRequest.getHeader(headerName);

            if (correlationId != null && !correlationId.trim().isEmpty()) {
                return correlationId.trim();
            }
        }

        if (generateIfMissing) {
            return UUID.randomUUID().toString();
        }

        return null;
    }

    /**
     * Returns the configured HTTP header name for the correlation ID.
     */
    public String getHeaderName() {
        return headerName;
    }

    /**
     * Returns the configured MDC key for the correlation ID.
     */
    public String getMdcKey() {
        return mdcKey;
    }
}
