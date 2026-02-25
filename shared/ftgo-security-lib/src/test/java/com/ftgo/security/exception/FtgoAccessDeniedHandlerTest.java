package com.ftgo.security.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link FtgoAccessDeniedHandler}.
 */
class FtgoAccessDeniedHandlerTest {

    private final FtgoAccessDeniedHandler handler = new FtgoAccessDeniedHandler();

    @Test
    @DisplayName("should return 403 status with JSON content type")
    void shouldReturn403WithJsonContentType() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/admin/settings");
        MockHttpServletResponse response = new MockHttpServletResponse();

        handler.handle(request, response, new AccessDeniedException("Access denied"));

        assertThat(response.getStatus()).isEqualTo(HttpStatus.FORBIDDEN.value());
        assertThat(response.getContentType()).isEqualTo("application/json");
    }

    @Test
    @DisplayName("should include error details in response body")
    void shouldIncludeErrorDetailsInResponseBody() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/admin/settings");
        MockHttpServletResponse response = new MockHttpServletResponse();

        handler.handle(request, response, new AccessDeniedException("Access denied"));

        String content = response.getContentAsString();
        assertThat(content).contains("\"status\":403");
        assertThat(content).contains("\"error\":\"Forbidden\"");
        assertThat(content).contains("\"path\":\"/api/admin/settings\"");
        assertThat(content).contains("\"message\":\"Access to this resource is denied\"");
    }
}
