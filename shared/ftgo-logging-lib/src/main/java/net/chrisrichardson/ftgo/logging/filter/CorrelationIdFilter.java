package net.chrisrichardson.ftgo.logging.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * Servlet filter that extracts or generates a correlation ID for each request
 * and places it in the SLF4J MDC for inclusion in structured log output.
 *
 * <p>The correlation ID is extracted from the configured HTTP header
 * (default: {@code X-Correlation-ID}). If the header is not present, a new
 * UUID-based correlation ID is generated.
 *
 * <p>The correlation ID is also set as a response header so downstream
 * consumers and API gateways can track the full request flow.
 *
 * <p>MDC keys populated by this filter:
 * <ul>
 *   <li>{@code correlationId} — the correlation ID for the request</li>
 *   <li>{@code requestMethod} — HTTP method (GET, POST, etc.)</li>
 *   <li>{@code requestUri} — request URI path</li>
 * </ul>
 */
public class CorrelationIdFilter extends OncePerRequestFilter {

    /** MDC key for the correlation ID. */
    public static final String MDC_CORRELATION_ID = "correlationId";

    /** MDC key for the HTTP request method. */
    public static final String MDC_REQUEST_METHOD = "requestMethod";

    /** MDC key for the HTTP request URI. */
    public static final String MDC_REQUEST_URI = "requestUri";

    private final String correlationIdHeader;

    /**
     * Creates a new CorrelationIdFilter with the specified header name.
     *
     * @param correlationIdHeader the HTTP header name for the correlation ID
     */
    public CorrelationIdFilter(String correlationIdHeader) {
        this.correlationIdHeader = correlationIdHeader;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                     HttpServletResponse response,
                                     FilterChain filterChain) throws ServletException, IOException {
        try {
            // Extract or generate correlation ID
            String correlationId = request.getHeader(correlationIdHeader);
            if (correlationId == null || correlationId.isBlank()) {
                correlationId = UUID.randomUUID().toString();
            }

            // Populate MDC
            MDC.put(MDC_CORRELATION_ID, correlationId);
            MDC.put(MDC_REQUEST_METHOD, request.getMethod());
            MDC.put(MDC_REQUEST_URI, request.getRequestURI());

            // Set correlation ID in response header for downstream tracking
            response.setHeader(correlationIdHeader, correlationId);

            filterChain.doFilter(request, response);
        } finally {
            // Clean up MDC to prevent leaking between requests
            MDC.remove(MDC_CORRELATION_ID);
            MDC.remove(MDC_REQUEST_METHOD);
            MDC.remove(MDC_REQUEST_URI);
        }
    }

    /**
     * Returns the configured correlation ID header name.
     */
    public String getCorrelationIdHeader() {
        return correlationIdHeader;
    }
}
