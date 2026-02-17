package net.chrisrichardson.ftgo.SERVICENAME;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration test template for FTGO REST controllers.
 * Replace SERVICENAME, ControllerClass, and endpoint paths.
 */
@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("ControllerClass Integration Tests")
class ControllerClassIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("GET /api/v1/entities - should return list")
    void should_returnList_when_getAll() throws Exception {
        mockMvc.perform(get("/api/v1/entities")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    @DisplayName("POST /api/v1/entities - should create entity")
    void should_createEntity_when_validPost() throws Exception {
        String json = """
                {
                    "name": "Test Entity",
                    "field": "value"
                }
                """;

        mockMvc.perform(post("/api/v1/entities")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists());
    }

    @Test
    @DisplayName("GET /api/v1/entities/{id} - should return 404 when not found")
    void should_return404_when_entityNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/entities/999")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("NOT_FOUND"));
    }

    @Test
    @DisplayName("POST /api/v1/entities - should return 400 on invalid input")
    void should_return400_when_invalidInput() throws Exception {
        String json = "{}";

        mockMvc.perform(post("/api/v1/entities")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
    }
}
