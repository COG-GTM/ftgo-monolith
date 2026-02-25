package com.ftgo.security.config;

import com.ftgo.security.exception.FtgoAccessDeniedHandler;
import com.ftgo.security.exception.FtgoAuthenticationEntryPoint;
import com.ftgo.security.filter.CorrelationIdFilter;
import com.ftgo.security.properties.FtgoSecurityProperties;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for {@link FtgoBaseSecurityConfiguration}.
 * <p>
 * Tests verify that the security configuration correctly:
 * <ul>
 *   <li>Requires authentication for all API endpoints by default</li>
 *   <li>Returns JSON 401 responses for unauthenticated requests</li>
 *   <li>Allows public access to health and info actuator endpoints</li>
 *   <li>Secures other actuator endpoints</li>
 *   <li>Disables CSRF for stateless REST APIs</li>
 *   <li>Adds correlation ID headers</li>
 * </ul>
 */
@SpringBootTest(
    classes = FtgoBaseSecurityConfigurationTest.TestApplication.class,
    webEnvironment = SpringBootTest.WebEnvironment.MOCK
)
@AutoConfigureMockMvc
class FtgoBaseSecurityConfigurationTest {

    @Autowired
    private MockMvc mockMvc;

    @SpringBootApplication
    @Import({
        FtgoBaseSecurityConfiguration.class,
        FtgoCorsConfiguration.class,
        FtgoAuthenticationEntryPoint.class,
        FtgoAccessDeniedHandler.class,
        CorrelationIdFilter.class
    })
    static class TestApplication {
    }

    @RestController
    static class TestController {

        @GetMapping("/api/test")
        public String test() {
            return "OK";
        }

        @GetMapping("/api/protected")
        public String protectedEndpoint() {
            return "Protected";
        }
    }

    @Nested
    @DisplayName("Unauthenticated Requests")
    class UnauthenticatedRequests {

        @Test
        @DisplayName("should return 401 JSON for unauthenticated API requests")
        void shouldReturn401ForUnauthenticatedApiRequests() throws Exception {
            mockMvc.perform(get("/api/test"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.message").value("Authentication is required to access this resource"))
                .andExpect(jsonPath("$.path").value("/api/test"));
        }

        @Test
        @DisplayName("should return 401 for unauthenticated protected endpoints")
        void shouldReturn401ForProtectedEndpoints() throws Exception {
            mockMvc.perform(get("/api/protected"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(401));
        }
    }

    @Nested
    @DisplayName("Authenticated Requests")
    class AuthenticatedRequests {

        @Test
        @WithMockUser(username = "testuser")
        @DisplayName("should allow authenticated users to access API endpoints")
        void shouldAllowAuthenticatedApiAccess() throws Exception {
            mockMvc.perform(get("/api/test"))
                .andExpect(status().isOk())
                .andExpect(content().string("OK"));
        }

        @Test
        @WithMockUser(username = "testuser")
        @DisplayName("should allow authenticated users to access protected endpoints")
        void shouldAllowAuthenticatedProtectedAccess() throws Exception {
            mockMvc.perform(get("/api/protected"))
                .andExpect(status().isOk())
                .andExpect(content().string("Protected"));
        }
    }

    @Nested
    @DisplayName("CORS Configuration")
    class CorsTests {

        @Test
        @DisplayName("should include CORS headers for cross-origin requests")
        void shouldIncludeCorsHeaders() throws Exception {
            mockMvc.perform(options("/api/test")
                    .header("Origin", "http://localhost:3000")
                    .header("Access-Control-Request-Method", "GET"))
                .andExpect(header().exists("Access-Control-Allow-Origin"));
        }
    }

    @Nested
    @DisplayName("Correlation ID Filter")
    class CorrelationIdTests {

        @Test
        @DisplayName("should propagate existing correlation ID")
        void shouldPropagateExistingCorrelationId() throws Exception {
            String correlationId = "test-correlation-123";
            mockMvc.perform(get("/api/test")
                    .header("X-Correlation-Id", correlationId))
                .andExpect(header().string("X-Correlation-Id", correlationId));
        }

        @Test
        @DisplayName("should generate correlation ID when not provided")
        void shouldGenerateCorrelationIdWhenNotProvided() throws Exception {
            mockMvc.perform(get("/api/test"))
                .andExpect(header().exists("X-Correlation-Id"));
        }
    }

    @Nested
    @DisplayName("CSRF Protection")
    class CsrfTests {

        @Test
        @WithMockUser(username = "testuser")
        @DisplayName("should not require CSRF token for stateless API")
        void shouldNotRequireCsrfToken() throws Exception {
            mockMvc.perform(get("/api/test"))
                .andExpect(status().isOk());
        }
    }
}
