package net.chrisrichardson.ftgo.security;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;

import static org.junit.jupiter.api.Assertions.*;

class SecurityExceptionHandlerTest {

    private final SecurityExceptionHandler handler = new SecurityExceptionHandler();

    @Test
    void commenceShouldReturn401WithJsonBody() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/orders");
        MockHttpServletResponse response = new MockHttpServletResponse();

        handler.commence(request, response, new BadCredentialsException("Bad credentials"));

        assertEquals(HttpStatus.UNAUTHORIZED.value(), response.getStatus());
        assertEquals("application/json", response.getContentType());
        String body = response.getContentAsString();
        assertTrue(body.contains("\"status\":401"));
        assertTrue(body.contains("\"error\":\"UNAUTHORIZED\""));
        assertTrue(body.contains("\"path\":\"/api/orders\""));
        assertTrue(body.contains("\"timestamp\""));
    }

    @Test
    void handleShouldReturn403WithJsonBody() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/admin");
        MockHttpServletResponse response = new MockHttpServletResponse();

        handler.handle(request, response, new AccessDeniedException("Access denied"));

        assertEquals(HttpStatus.FORBIDDEN.value(), response.getStatus());
        assertEquals("application/json", response.getContentType());
        String body = response.getContentAsString();
        assertTrue(body.contains("\"status\":403"));
        assertTrue(body.contains("\"error\":\"FORBIDDEN\""));
        assertTrue(body.contains("\"path\":\"/api/admin\""));
    }
}
