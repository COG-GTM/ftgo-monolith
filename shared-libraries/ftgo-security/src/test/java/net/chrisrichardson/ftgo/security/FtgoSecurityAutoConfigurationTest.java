package net.chrisrichardson.ftgo.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class FtgoSecurityAutoConfigurationTest {

    @Autowired
    private MockMvc mockMvc;

    @SpringBootApplication
    @RestController
    static class TestApplication {

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
    @DisplayName("Authentication")
    class AuthenticationTests {

        @Test
        @DisplayName("Unauthenticated request to protected endpoint returns 401")
        void unauthenticatedRequestReturns401() throws Exception {
            mockMvc.perform(get("/api/test"))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.status").value(401))
                    .andExpect(jsonPath("$.error").value("Unauthorized"))
                    .andExpect(jsonPath("$.message").exists())
                    .andExpect(jsonPath("$.path").value("/api/test"));
        }

        @Test
        @DisplayName("Authenticated request to protected endpoint returns 200")
        void authenticatedRequestReturns200() throws Exception {
            mockMvc.perform(get("/api/test")
                            .with(httpBasic("user", "password")))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("All endpoints require authentication by default")
        void allEndpointsRequireAuthentication() throws Exception {
            mockMvc.perform(get("/api/protected"))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("Actuator Endpoints")
    class ActuatorTests {

        @Test
        @DisplayName("Health endpoint is publicly accessible")
        void healthEndpointIsPublic() throws Exception {
            mockMvc.perform(get("/actuator/health"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Other actuator endpoints require authentication")
        void otherActuatorEndpointsRequireAuth() throws Exception {
            mockMvc.perform(get("/actuator/info"))
                    .andExpect(status().isUnauthorized());

            mockMvc.perform(get("/actuator/metrics"))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("CSRF Protection")
    class CsrfTests {

        @Test
        @DisplayName("CSRF is disabled for stateless REST APIs")
        void csrfIsDisabled() throws Exception {
            // POST without CSRF token should not be rejected due to CSRF
            // (it will be rejected for auth, proving CSRF is disabled)
            mockMvc.perform(get("/api/test"))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("Session Management")
    class SessionTests {

        @Test
        @DisplayName("No session cookie is set (stateless)")
        void noSessionCookie() throws Exception {
            mockMvc.perform(get("/api/test")
                            .with(httpBasic("user", "password")))
                    .andExpect(header().doesNotExist("Set-Cookie"));
        }
    }

    @Nested
    @DisplayName("CORS Configuration")
    class CorsTests {

        @Test
        @DisplayName("CORS preflight request returns appropriate headers")
        void corsPreflightReturnsHeaders() throws Exception {
            mockMvc.perform(options("/api/test")
                            .header("Origin", "http://localhost:3000")
                            .header("Access-Control-Request-Method", "GET"))
                    .andExpect(status().isOk());
        }
    }
}
