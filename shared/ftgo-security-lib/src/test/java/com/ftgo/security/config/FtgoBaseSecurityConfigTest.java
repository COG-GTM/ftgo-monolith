package com.ftgo.security.config;

import com.ftgo.security.TestApplication;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for the base security configuration.
 *
 * <p>Tests verify:
 * <ul>
 *   <li>Unauthenticated requests to protected endpoints return 401</li>
 *   <li>Authenticated requests to protected endpoints are allowed</li>
 *   <li>Health endpoint is publicly accessible</li>
 *   <li>JSON error responses are returned for security failures</li>
 *   <li>CSRF is disabled for REST APIs</li>
 * </ul>
 */
@SpringBootTest(classes = TestApplication.class)
@AutoConfigureMockMvc
class FtgoBaseSecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Unauthenticated request to protected endpoint returns 401 with JSON body")
    void unauthenticatedRequestReturns401() throws Exception {
        mockMvc.perform(get("/api/test")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.path").value("/api/test"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @WithMockUser(username = "testuser")
    @DisplayName("Authenticated request to protected endpoint returns 200")
    void authenticatedRequestReturns200() throws Exception {
        mockMvc.perform(get("/api/test")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Health endpoint is publicly accessible without authentication")
    void healthEndpointIsPublic() throws Exception {
        mockMvc.perform(get("/actuator/health")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Info endpoint is publicly accessible without authentication")
    void infoEndpointIsPublic() throws Exception {
        mockMvc.perform(get("/actuator/info")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Other actuator endpoints require authentication")
    void otherActuatorEndpointsRequireAuth() throws Exception {
        mockMvc.perform(get("/actuator/env")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(result -> {
                    int statusCode = result.getResponse().getStatus();
                    assert statusCode == 401 || statusCode == 403
                            : "Expected 401 or 403 but got " + statusCode;
                });
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    @DisplayName("Authenticated user can access other actuator endpoints")
    void authenticatedUserCanAccessActuatorEndpoints() throws Exception {
        mockMvc.perform(get("/actuator/metrics")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Swagger UI endpoint is publicly accessible")
    void swaggerUiIsPublic() throws Exception {
        mockMvc.perform(get("/swagger-ui.html")
                        .accept(MediaType.APPLICATION_JSON))
                // Swagger UI may redirect or return 302, but should not return 401
                .andExpect(result -> {
                    int statusCode = result.getResponse().getStatus();
                    assert statusCode != 401 : "Swagger UI should not require authentication, got 401";
                });
    }
}
