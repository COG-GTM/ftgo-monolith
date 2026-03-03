package net.chrisrichardson.ftgo.security.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Custom access denied handler that returns a JSON error response
 * when an authenticated user attempts to access a resource they are not
 * authorized to access.
 *
 * <p>Returns HTTP 403 Forbidden with a structured JSON body:</p>
 * <pre>
 * {
 *   "timestamp": "2024-01-01T00:00:00Z",
 *   "status": 403,
 *   "error": "Forbidden",
 *   "message": "Access denied: insufficient permissions",
 *   "path": "/api/admin/settings"
 * }
 * </pre>
 */
public class FtgoAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {
        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", Instant.now().toString());
        body.put("status", HttpStatus.FORBIDDEN.value());
        body.put("error", "Forbidden");
        body.put("message", "Access denied: insufficient permissions");
        body.put("path", request.getRequestURI());

        objectMapper.writeValue(response.getOutputStream(), body);
    }
}
