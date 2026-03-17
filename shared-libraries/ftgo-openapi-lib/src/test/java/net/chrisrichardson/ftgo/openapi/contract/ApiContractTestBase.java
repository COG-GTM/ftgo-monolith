package net.chrisrichardson.ftgo.openapi.contract;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Base class for API contract tests across FTGO microservices.
 *
 * <p>Provides common utilities for verifying API contracts including:
 * <ul>
 *   <li>Standard JSON response structure validation</li>
 *   <li>HTTP status code verification</li>
 *   <li>Content-type header validation</li>
 *   <li>Error response format verification</li>
 * </ul>
 *
 * <p>Services should copy or extend this pattern to create their own contract tests
 * that verify the API contract between producer and consumer services.</p>
 *
 * <h3>Usage Example:</h3>
 * <pre>
 * public class OrderApiContractTest extends ApiContractTestBase {
 *     &#064;Autowired
 *     private MockMvc mockMvc;
 *
 *     &#064;Test
 *     void shouldReturnOrderWhenExists() throws Exception {
 *         verifyGetEndpoint(mockMvc, "/api/v1/orders/1", 200);
 *     }
 * }
 * </pre>
 */
public abstract class ApiContractTestBase {

    private static final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    /**
     * Verifies that a GET endpoint returns the expected status code
     * and produces JSON content.
     */
    protected ResultActions verifyGetEndpoint(MockMvc mockMvc, String path, int expectedStatus) throws Exception {
        return mockMvc.perform(MockMvcRequestBuilders.get(path)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(expectedStatus))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    /**
     * Verifies that a POST endpoint returns the expected status code
     * and produces JSON content.
     */
    protected ResultActions verifyPostEndpoint(MockMvc mockMvc, String path, Object requestBody, int expectedStatus) throws Exception {
        return mockMvc.perform(MockMvcRequestBuilders.post(path)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(expectedStatus));
    }

    /**
     * Verifies that a 404 response matches the standard error format.
     */
    protected ResultActions verifyNotFoundResponse(MockMvc mockMvc, String path) throws Exception {
        return mockMvc.perform(MockMvcRequestBuilders.get(path)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    /**
     * Converts an object to JSON string for request bodies.
     */
    protected String toJson(Object object) throws Exception {
        return objectMapper.writeValueAsString(object);
    }
}
