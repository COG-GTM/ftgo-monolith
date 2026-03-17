package net.chrisrichardson.ftgo.security.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for CORS configuration.
 */
@SpringBootTest(classes = TestSecurityApplication.class)
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "spring.security.user.name=testuser",
        "spring.security.user.password=testpass",
        "ftgo.security.cors.allowed-origins=http://localhost:3000,http://api-gateway.ftgo.svc"
})
class FtgoCorsConfigurationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void corsPreflightReturnsAllowedOrigin() throws Exception {
        mockMvc.perform(options("/api/test")
                        .header("Origin", "http://localhost:3000")
                        .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:3000"));
    }

    @Test
    void corsPreflightReturnsAllowedMethods() throws Exception {
        mockMvc.perform(options("/api/test")
                        .header("Origin", "http://localhost:3000")
                        .header("Access-Control-Request-Method", "POST"))
                .andExpect(status().isOk())
                .andExpect(header().exists("Access-Control-Allow-Methods"));
    }

    @Test
    void corsHeadersIncludedInAuthenticatedResponse() throws Exception {
        mockMvc.perform(get("/api/test")
                        .header("Origin", "http://localhost:3000")
                        .with(httpBasic("testuser", "testpass")))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:3000"));
    }

    @Test
    void corsRejectsDisallowedOrigin() throws Exception {
        mockMvc.perform(options("/api/test")
                        .header("Origin", "http://malicious-site.com")
                        .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isForbidden());
    }
}
