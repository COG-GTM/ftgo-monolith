package net.chrisrichardson.ftgo.security;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest
@TestPropertySource(properties = {"ftgo.api.key=test-api-key"})
public class ApiKeyAuthenticationFilterTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ApiKeyAuthenticationFilter apiKeyAuthenticationFilter;

    @Test
    public void testHealthEndpointAccessibleWithoutApiKey() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk());
    }

    @Test
    public void testProtectedEndpointRequiresApiKey() throws Exception {
        mockMvc.perform(get("/orders"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testProtectedEndpointWithValidApiKey() throws Exception {
        mockMvc.perform(get("/orders")
                .header("X-API-Key", "test-api-key"))
                .andExpect(status().isOk());
    }

    @Test
    public void testProtectedEndpointWithInvalidApiKey() throws Exception {
        mockMvc.perform(get("/orders")
                .header("X-API-Key", "invalid-key"))
                .andExpected(status().isUnauthorized());
    }
}
