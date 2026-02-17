package net.chrisrichardson.ftgo.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;

/**
 * Handles security exceptions with consistent JSON error responses.
 */
@Component
public class SecurityExceptionHandler implements AuthenticationEntryPoint, AccessDeniedHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        sendErrorResponse(response, HttpStatus.UNAUTHORIZED, "UNAUTHORIZED",
                "Authentication required", request.getRequestURI());
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {
        sendErrorResponse(response, HttpStatus.FORBIDDEN, "FORBIDDEN",
                "Insufficient permissions", request.getRequestURI());
    }

    private void sendErrorResponse(HttpServletResponse response, HttpStatus status,
                                   String error, String message, String path) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        Map<String, Object> body = Map.of(
                "status", status.value(),
                "error", error,
                "message", message,
                "timestamp", Instant.now().toString(),
                "path", path
        );

        objectMapper.writeValue(response.getOutputStream(), body);
    }
}
