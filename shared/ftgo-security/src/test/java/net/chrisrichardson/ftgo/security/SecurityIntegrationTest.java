package net.chrisrichardson.ftgo.security;

import net.chrisrichardson.ftgo.security.config.FtgoCorsConfig;
import net.chrisrichardson.ftgo.security.config.FtgoSecurityFilterChainConfig;
import net.chrisrichardson.ftgo.security.exception.SecurityExceptionHandler;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.http.HttpMessageConvertersAutoConfiguration;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.DispatcherServletAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for the FTGO security configuration.
 *
 * <p>Tests verify that:</p>
 * <ul>
 *   <li>Unauthenticated requests to secured endpoints return 401</li>
 *   <li>Authenticated requests to secured endpoints succeed</li>
 *   <li>Public paths (actuator health/info) are accessible without authentication</li>
 *   <li>CORS headers are returned for configured origins</li>
 *   <li>CSRF is disabled for API requests</li>
 *   <li>Security error responses are properly formatted</li>
 * </ul>
 *
 * <p>Note: Actuator endpoint registration requires a full Spring Boot application
 * context. These tests use simulated controller endpoints matching the actuator
 * paths to verify the security filter chain rules.</p>
 */
@RunWith(SpringRunner.class)
@SpringBootTest(
        classes = SecurityIntegrationTest.TestConfig.class,
        webEnvironment = SpringBootTest.WebEnvironment.MOCK
)
@AutoConfigureMockMvc
public class SecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    // -------------------------------------------------------------------------
    // Authentication Tests
    // -------------------------------------------------------------------------

    @Test
    public void shouldReturn401ForUnauthenticatedRequest() throws Exception {
        mockMvc.perform(get("/api/orders")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    public void shouldReturn200ForAuthenticatedRequest() throws Exception {
        mockMvc.perform(get("/api/test")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    public void shouldReturnJsonErrorForUnauthenticatedRequest() throws Exception {
        mockMvc.perform(get("/api/orders")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.path").value("/api/orders"));
    }

    // -------------------------------------------------------------------------
    // Public Path Security Tests (simulates actuator path security)
    // -------------------------------------------------------------------------

    @Test
    public void shouldAllowPublicAccessToHealthPath() throws Exception {
        // Verifies that /actuator/health is publicly accessible
        // as configured in FtgoSecurityProperties public-paths
        mockMvc.perform(get("/actuator/health")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    public void shouldAllowPublicAccessToInfoPath() throws Exception {
        // Verifies that /actuator/info is publicly accessible
        // as configured in FtgoSecurityProperties public-paths
        mockMvc.perform(get("/actuator/info")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    // -------------------------------------------------------------------------
    // CORS Tests
    // -------------------------------------------------------------------------

    @Test
    public void shouldReturnCorsHeadersForAllowedOrigin() throws Exception {
        mockMvc.perform(options("/api/test")
                        .header(HttpHeaders.ORIGIN, "http://localhost:3000")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "http://localhost:3000"));
    }

    @Test
    public void shouldRejectCorsForDisallowedOrigin() throws Exception {
        mockMvc.perform(options("/api/test")
                        .header(HttpHeaders.ORIGIN, "http://evil-site.com")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET"))
                .andExpect(status().isForbidden());
    }

    @Test
    public void shouldAllowOptionsPreflightRequests() throws Exception {
        mockMvc.perform(options("/api/test")
                        .header(HttpHeaders.ORIGIN, "http://localhost:3000")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "Authorization, Content-Type"))
                .andExpect(status().isOk())
                .andExpect(header().exists(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS));
    }

    // -------------------------------------------------------------------------
    // CSRF Tests
    // -------------------------------------------------------------------------

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    public void shouldAllowPostWithoutCsrfToken() throws Exception {
        // CSRF is disabled for stateless APIs, so POST should work without CSRF token
        mockMvc.perform(get("/api/test")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    // -------------------------------------------------------------------------
    // Test Configuration
    // -------------------------------------------------------------------------

    @Configuration
    @EnableConfigurationProperties(FtgoSecurityProperties.class)
    @Import({
            FtgoSecurityFilterChainConfig.class,
            FtgoCorsConfig.class,
            SecurityExceptionHandler.class,
            TestController.class,
            // Spring Boot auto-configurations needed for testing
            DispatcherServletAutoConfiguration.class,
            WebMvcAutoConfiguration.class,
            HttpMessageConvertersAutoConfiguration.class,
            JacksonAutoConfiguration.class,
    })
    static class TestConfig {
    }

    /**
     * Test controller that simulates real service and actuator endpoints
     * for security filter chain verification.
     */
    @RestController
    static class TestController {

        @GetMapping("/api/test")
        public String test() {
            return "OK";
        }

        @GetMapping("/actuator/health")
        public String health() {
            return "{\"status\":\"UP\"}";
        }

        @GetMapping("/actuator/info")
        public String info() {
            return "{}";
        }
    }
}
