package net.chrisrichardson.ftgo.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class FtgoSecurityConfigurationIntegrationTest {

    @SpringBootApplication
    @RestController
    static class TestApp {
        @GetMapping("/api/test")
        public String securedEndpoint() {
            return "secured";
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Test
    void actuatorHealthEndpointShouldBePublic() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk());
    }

    @Test
    void actuatorInfoEndpointShouldBePublic() throws Exception {
        mockMvc.perform(get("/actuator/info"))
                .andExpect(status().isOk());
    }

    @Test
    void securedEndpointShouldReturn401WithoutAuth() throws Exception {
        mockMvc.perform(get("/api/test"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("UNAUTHORIZED"))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.path").value("/api/test"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void swaggerUiEndpointShouldNotRequireAuth() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isNotFound());
    }

    @Test
    void csrfShouldBeDisabledForStatelessApis() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(cookie().doesNotExist("XSRF-TOKEN"));
    }

    @Test
    void sessionShouldNotBeCreated() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(header().doesNotExist("Set-Cookie"));
    }

    @Test
    void corsPreflightRequestShouldSucceed() throws Exception {
        mockMvc.perform(options("/api/test")
                        .header("Origin", "http://localhost:3000")
                        .header("Access-Control-Request-Method", "GET")
                        .header("Access-Control-Request-Headers", "Authorization"))
                .andExpect(status().isOk())
                .andExpect(header().exists("Access-Control-Allow-Origin"))
                .andExpect(header().exists("Access-Control-Allow-Methods"));
    }

    @Test
    void corsResponseShouldIncludeExpectedHeaders() throws Exception {
        mockMvc.perform(options("/api/test")
                        .header("Origin", "http://localhost:3000")
                        .header("Access-Control-Request-Method", "POST")
                        .header("Access-Control-Request-Headers", "Content-Type"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:3000"))
                .andExpect(header().string("Access-Control-Allow-Credentials", "true"));
    }

    @Test
    void otherActuatorEndpointsShouldRequireAuth() throws Exception {
        mockMvc.perform(get("/actuator/env"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void prometheusEndpointShouldNotRequireAuth() throws Exception {
        mockMvc.perform(get("/actuator/prometheus"))
                .andExpect(status().isNotFound());
    }
}
