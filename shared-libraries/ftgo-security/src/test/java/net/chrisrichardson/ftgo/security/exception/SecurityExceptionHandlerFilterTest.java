package net.chrisrichardson.ftgo.security.exception;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

/**
 * Unit tests for {@link SecurityExceptionHandlerFilter}.
 */
class SecurityExceptionHandlerFilterTest {

    private final SecurityExceptionHandlerFilter filter = new SecurityExceptionHandlerFilter();

    @Test
    void normalRequestPassesThrough() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/test");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilterInternal(request, response, chain);

        assertEquals(HttpStatus.OK.value(), response.getStatus());
    }

    @Test
    void runtimeExceptionInFilterChainReturnsJsonError() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/test");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);
        doThrow(new RuntimeException("Test error")).when(chain).doFilter(request, response);

        filter.doFilterInternal(request, response, chain);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), response.getStatus());
        String body = response.getContentAsString();
        assertTrue(body.contains("\"status\":500"));
        assertTrue(body.contains("\"path\":\"/api/test\""));
        assertTrue(body.contains("An internal security error occurred"));
    }

    @Test
    void servletExceptionIsHandled() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/data");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);
        doThrow(new ServletException("Servlet error")).when(chain).doFilter(request, response);

        filter.doFilterInternal(request, response, chain);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), response.getStatus());
        String body = response.getContentAsString();
        assertTrue(body.contains("\"path\":\"/api/data\""));
    }
}
