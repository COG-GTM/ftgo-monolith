package net.chrisrichardson.ftgo.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class SecurityExceptionHandlerTest {

    private final SecurityExceptionHandler handler = new SecurityExceptionHandler();

    @Test
    @DisplayName("handleAuthenticationException returns 401 with JSON body")
    void handleAuthenticationException_returns401() {
        ResponseEntity<Map<String, Object>> response =
                handler.handleAuthenticationException(new BadCredentialsException("Bad credentials"));

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(401, response.getBody().get("status"));
        assertEquals("Unauthorized", response.getBody().get("error"));
        assertNotNull(response.getBody().get("timestamp"));
        assertNotNull(response.getBody().get("message"));
    }

    @Test
    @DisplayName("handleAccessDeniedException returns 403 with JSON body")
    void handleAccessDeniedException_returns403() {
        ResponseEntity<Map<String, Object>> response =
                handler.handleAccessDeniedException(new AccessDeniedException("Access denied"));

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(403, response.getBody().get("status"));
        assertEquals("Forbidden", response.getBody().get("error"));
        assertNotNull(response.getBody().get("timestamp"));
        assertNotNull(response.getBody().get("message"));
    }
}
