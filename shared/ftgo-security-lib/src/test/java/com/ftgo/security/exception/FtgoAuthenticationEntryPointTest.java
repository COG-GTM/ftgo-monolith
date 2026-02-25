package com.ftgo.security.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link FtgoAuthenticationEntryPoint}.
 */
class FtgoAuthenticationEntryPointTest {

    private final FtgoAuthenticationEntryPoint entryPoint = new FtgoAuthenticationEntryPoint();

    @Test
    @DisplayName("should return 401 status with JSON content type")
    void shouldReturn401WithJsonContentType() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/orders");
        MockHttpServletResponse response = new MockHttpServletResponse();

        entryPoint.commence(request, response, new BadCredentialsException("Bad credentials"));

        assertThat(response.getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
        assertThat(response.getContentType()).isEqualTo("application/json");
    }

    @Test
    @DisplayName("should include error details in response body")
    void shouldIncludeErrorDetailsInResponseBody() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/orders");
        MockHttpServletResponse response = new MockHttpServletResponse();

        entryPoint.commence(request, response, new BadCredentialsException("Bad credentials"));

        String content = response.getContentAsString();
        assertThat(content).contains("\"status\":401");
        assertThat(content).contains("\"error\":\"Unauthorized\"");
        assertThat(content).contains("\"path\":\"/api/orders\"");
        assertThat(content).contains("\"message\":\"Authentication is required to access this resource\"");
    }
}
