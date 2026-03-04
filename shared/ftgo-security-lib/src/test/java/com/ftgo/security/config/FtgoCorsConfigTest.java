package com.ftgo.security.config;

import com.ftgo.security.TestApplication;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for CORS configuration.
 *
 * <p>Tests verify that CORS headers are correctly set for pre-flight
 * and actual requests from allowed origins.
 */
@SpringBootTest(classes = TestApplication.class)
@AutoConfigureMockMvc
class FtgoCorsConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("CORS pre-flight request from allowed origin returns correct headers")
    void corsPreflightFromAllowedOrigin() throws Exception {
        mockMvc.perform(options("/api/test")
                        .header("Origin", "http://localhost:3000")
                        .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:3000"))
                .andExpect(header().exists("Access-Control-Allow-Methods"));
    }

    @Test
    @DisplayName("CORS pre-flight request from disallowed origin is rejected")
    void corsPreflightFromDisallowedOrigin() throws Exception {
        mockMvc.perform(options("/api/test")
                        .header("Origin", "http://evil.example.com")
                        .header("Access-Control-Request-Method", "GET"))
                .andExpect(header().doesNotExist("Access-Control-Allow-Origin"));
    }

    @Test
    @WithMockUser
    @DisplayName("Actual request from allowed origin includes CORS headers")
    void actualRequestFromAllowedOriginIncludesCorsHeaders() throws Exception {
        mockMvc.perform(get("/api/test")
                        .header("Origin", "http://localhost:3000"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:3000"));
    }
}
