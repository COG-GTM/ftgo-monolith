package net.chrisrichardson.ftgo.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
 * The correlation ID is propagated via MDC for structured logging and added
 * as a response header for downstream tracing.
 */
public class CorrelationIdFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(CorrelationIdFilter.class);

    private final String serviceName;

    public CorrelationIdFilter(String serviceName) {
        this.serviceName = serviceName;
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // No initialization required
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String correlationId = httpRequest.getHeader(LoggingConstants.CORRELATION_ID_HEADER);
        if (correlationId == null || correlationId.trim().isEmpty()) {
            correlationId = UUID.randomUUID().toString();
        }

        try {
            MDC.put(LoggingConstants.MDC_CORRELATION_ID, correlationId);
            MDC.put(LoggingConstants.MDC_SERVICE_NAME, serviceName);
            MDC.put(LoggingConstants.MDC_REQUEST_METHOD, httpRequest.getMethod());
            MDC.put(LoggingConstants.MDC_REQUEST_URI, httpRequest.getRequestURI());
            MDC.put(LoggingConstants.MDC_CLIENT_IP, getClientIp(httpRequest));

            httpResponse.setHeader(LoggingConstants.CORRELATION_ID_HEADER, correlationId);

            logger.debug("Request started: {} {}", httpRequest.getMethod(), httpRequest.getRequestURI());
            chain.doFilter(request, response);
            logger.debug("Request completed: {} {} - status {}",
                    httpRequest.getMethod(), httpRequest.getRequestURI(), httpResponse.getStatus());

        } finally {
            MDC.remove(LoggingConstants.MDC_CORRELATION_ID);
            MDC.remove(LoggingConstants.MDC_SERVICE_NAME);
            MDC.remove(LoggingConstants.MDC_REQUEST_METHOD);
            MDC.remove(LoggingConstants.MDC_REQUEST_URI);
            MDC.remove(LoggingConstants.MDC_CLIENT_IP);
            MDC.remove(LoggingConstants.MDC_USER_ID);
        }
    }

    @Override
    public void destroy() {
        // No cleanup required
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
