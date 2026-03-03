package com.ftgo.logging.filter;

import com.ftgo.logging.CorrelationIdGenerator;
import com.ftgo.logging.LoggingConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Servlet filter that extracts or generates correlation IDs for every incoming
 * HTTP request and populates the SLF4J MDC (Mapped Diagnostic Context).
 *
 * <p>If the incoming request contains an {@code X-Correlation-ID} header, that
 * value is reused. Otherwise, a new UUID-based correlation ID is generated.
 * The correlation ID is also set as a response header for downstream tracing.</p>
 *
 * <p>Additional request context (method, URI, client IP, request ID) is also
 * added to the MDC so that all log entries within the request lifecycle include
 * this information in structured JSON output.</p>
 *
 * <p>This filter runs at the highest precedence to ensure MDC is populated
 * before any other filters or controllers log messages.</p>
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CorrelationIdFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(CorrelationIdFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            // Extract or generate correlation ID
            String correlationId = request.getHeader(LoggingConstants.HEADER_CORRELATION_ID);
            if (correlationId == null || correlationId.trim().isEmpty()) {
                correlationId = CorrelationIdGenerator.generate();
            }

            // Extract or generate request ID
            String requestId = request.getHeader(LoggingConstants.HEADER_REQUEST_ID);
            if (requestId == null || requestId.trim().isEmpty()) {
                requestId = CorrelationIdGenerator.generate();
            }

            // Populate MDC with request context
            MDC.put(LoggingConstants.MDC_CORRELATION_ID, correlationId);
            MDC.put(LoggingConstants.MDC_REQUEST_ID, requestId);
            MDC.put(LoggingConstants.MDC_REQUEST_METHOD, request.getMethod());
            MDC.put(LoggingConstants.MDC_REQUEST_URI, request.getRequestURI());
            MDC.put(LoggingConstants.MDC_CLIENT_IP, getClientIp(request));

            // Set correlation ID on response for downstream services
            response.setHeader(LoggingConstants.HEADER_CORRELATION_ID, correlationId);
            response.setHeader(LoggingConstants.HEADER_REQUEST_ID, requestId);

            log.debug("Request started: {} {} [correlationId={}]",
                    request.getMethod(), request.getRequestURI(), correlationId);

            filterChain.doFilter(request, response);
        } finally {
            // Clean up MDC to prevent context leaking between requests
            MDC.remove(LoggingConstants.MDC_CORRELATION_ID);
            MDC.remove(LoggingConstants.MDC_REQUEST_ID);
            MDC.remove(LoggingConstants.MDC_REQUEST_METHOD);
            MDC.remove(LoggingConstants.MDC_REQUEST_URI);
            MDC.remove(LoggingConstants.MDC_CLIENT_IP);
        }
    }

    /**
     * Extracts the client IP address, accounting for reverse proxies.
     */
    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            // Take the first IP in the chain (original client)
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
