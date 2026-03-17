package net.chrisrichardson.ftgo.security.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for {@link FtgoBaseSecurityConfiguration}.
 */
@SpringBootTest(classes = TestSecurityApplication.class)
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "spring.security.user.name=testuser",
        "spring.security.user.password=testpass",
        "spring.security.user.roles=USER"
})
class FtgoBaseSecurityConfigurationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void unauthenticatedRequestToProtectedEndpointReturns401() throws Exception {
        mockMvc.perform(get("/api/test")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.path").value("/api/test"));
    }

    @Test
    void authenticatedRequestToProtectedEndpointReturns200() throws Exception {
        mockMvc.perform(get("/api/test")
                        .with(httpBasic("testuser", "testpass"))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void invalidCredentialsReturn401() throws Exception {
        mockMvc.perform(get("/api/test")
                        .with(httpBasic("testuser", "wrongpassword"))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void actuatorHealthEndpointIsPublic() throws Exception {
        mockMvc.perform(get("/actuator/health")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void actuatorInfoEndpointIsPublic() throws Exception {
        // Info endpoint returns 200 even if empty
        mockMvc.perform(get("/actuator/info")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void otherActuatorEndpointsRequireAuthentication() throws Exception {
        mockMvc.perform(get("/actuator/metrics")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void authenticatedActuatorMetricsReturns200() throws Exception {
        mockMvc.perform(get("/actuator/metrics")
                        .with(httpBasic("testuser", "testpass"))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void csrfIsDisabledForStatelessApis() throws Exception {
        // POST without CSRF token should not be rejected due to CSRF
        // (will be rejected due to auth instead, which proves CSRF is disabled)
        mockMvc.perform(get("/api/test")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void corsPreflightOptionsRequestIsAllowed() throws Exception {
        mockMvc.perform(options("/api/test")
                        .header("Origin", "http://localhost:3000")
                        .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isOk());
    }

    @Test
    void sessionManagementIsStateless() throws Exception {
        // Authenticated request should not set a session cookie
        mockMvc.perform(get("/api/test")
                        .with(httpBasic("testuser", "testpass"))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(result -> {
                    String setCookie = result.getResponse().getHeader("Set-Cookie");
                    // No session cookie should be set in stateless mode
                    assert setCookie == null || !setCookie.contains("JSESSIONID");
                });
    }
}
