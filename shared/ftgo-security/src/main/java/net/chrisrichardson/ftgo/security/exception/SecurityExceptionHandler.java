package net.chrisrichardson.ftgo.security.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Global exception handler for security-related exceptions.
 *
 * <p>Handles {@link AuthenticationException} and {@link AccessDeniedException}
 * that escape the Spring Security filter chain and reach the controller layer.
 * Returns structured JSON error responses consistent with the custom entry point
 * and access denied handler.</p>
 */
@RestControllerAdvice
public class SecurityExceptionHandler {

    /**
     * Handles authentication exceptions (401 Unauthorized).
     *
     * @param ex the authentication exception
     * @param request the HTTP request
     * @return a structured error response
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Map<String, Object>> handleAuthenticationException(
            AuthenticationException ex, HttpServletRequest request) {
        Map<String, Object> body = createErrorBody(
                HttpStatus.UNAUTHORIZED,
                "Unauthorized",
                resolveAuthenticationMessage(ex),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
    }

    /**
     * Handles access denied exceptions (403 Forbidden).
     *
     * @param ex the access denied exception
     * @param request the HTTP request
     * @return a structured error response
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDeniedException(
            AccessDeniedException ex, HttpServletRequest request) {
        Map<String, Object> body = createErrorBody(
                HttpStatus.FORBIDDEN,
                "Forbidden",
                "Access denied: insufficient permissions",
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body);
    }

    private String resolveAuthenticationMessage(AuthenticationException ex) {
        if (ex instanceof BadCredentialsException) {
            return "Invalid credentials provided";
        }
        if (ex instanceof InsufficientAuthenticationException) {
            return "Full authentication is required to access this resource";
        }
        return "Authentication is required to access this resource";
    }

    private Map<String, Object> createErrorBody(HttpStatus status, String error,
                                                 String message, String path) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", Instant.now().toString());
        body.put("status", status.value());
        body.put("error", error);
        body.put("message", message);
        body.put("path", path);
        return body;
    }
}
