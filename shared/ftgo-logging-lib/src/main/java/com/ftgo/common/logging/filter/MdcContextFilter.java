package com.ftgo.common.logging.filter;

import org.slf4j.MDC;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * Servlet filter that enriches the MDC with request context.
 *
 * <p>This filter adds the following MDC fields for every request:</p>
 * <ul>
 *   <li>{@code service} - The application/service name</li>
 *   <li>{@code requestMethod} - HTTP method (GET, POST, etc.)</li>
 *   <li>{@code requestUri} - The request URI path</li>
 *   <li>{@code remoteAddr} - Client IP address</li>
 *   <li>{@code userAgent} - Client User-Agent header</li>
 * </ul>
 *
 * <p>These fields are automatically included in structured JSON log
 * output and can be used for filtering and searching in Kibana.</p>
 */
public class MdcContextFilter implements Filter {

    /** MDC key for the service name. */
    public static final String MDC_SERVICE = "service";
    /** MDC key for the HTTP request method. */
    public static final String MDC_REQUEST_METHOD = "requestMethod";
    /** MDC key for the request URI. */
    public static final String MDC_REQUEST_URI = "requestUri";
    /** MDC key for the remote client address. */
    public static final String MDC_REMOTE_ADDR = "remoteAddr";
    /** MDC key for the User-Agent header. */
    public static final String MDC_USER_AGENT = "userAgent";

    private final String serviceName;

    public MdcContextFilter(String serviceName) {
        this.serviceName = serviceName;
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // No initialization needed
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        try {
            MDC.put(MDC_SERVICE, serviceName);

            if (request instanceof HttpServletRequest) {
                HttpServletRequest httpRequest = (HttpServletRequest) request;
                MDC.put(MDC_REQUEST_METHOD, httpRequest.getMethod());
                MDC.put(MDC_REQUEST_URI, httpRequest.getRequestURI());
                MDC.put(MDC_REMOTE_ADDR, getClientIpAddress(httpRequest));

                String userAgent = httpRequest.getHeader("User-Agent");
                if (userAgent != null) {
                    MDC.put(MDC_USER_AGENT, userAgent);
                }
            }

            chain.doFilter(request, response);
        } finally {
            MDC.remove(MDC_SERVICE);
            MDC.remove(MDC_REQUEST_METHOD);
            MDC.remove(MDC_REQUEST_URI);
            MDC.remove(MDC_REMOTE_ADDR);
            MDC.remove(MDC_USER_AGENT);
        }
    }

    @Override
    public void destroy() {
        // No cleanup needed
    }

    /**
     * Resolves the client IP address, considering X-Forwarded-For proxy headers.
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            // Take the first IP (original client) from the comma-separated list
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    /**
     * Returns the configured service name.
     */
    public String getServiceName() {
        return serviceName;
    }
}
