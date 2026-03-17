package net.chrisrichardson.ftgo.security.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Filter that catches unexpected exceptions during security filter chain execution
 * and returns a consistent JSON error response.
 *
 * <p>This filter is placed before the authentication filter to catch any exceptions
 * that occur during the security processing pipeline.
 */
@Component
public class SecurityExceptionHandlerFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(SecurityExceptionHandlerFilter.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws IOException {
        try {
            filterChain.doFilter(request, response);
        } catch (Exception ex) {
            log.error("Unexpected security error for request {} {}: {}",
                    request.getMethod(), request.getRequestURI(), ex.getMessage(), ex);
            writeErrorResponse(request, response, ex);
        }
    }

    private void writeErrorResponse(HttpServletRequest request,
                                    HttpServletResponse response,
                                    Exception ex) throws IOException {
        response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", Instant.now().toString());
        body.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        body.put("error", HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase());
        body.put("message", "An internal security error occurred");
        body.put("path", request.getRequestURI());

        objectMapper.writeValue(response.getOutputStream(), body);
    }
}
