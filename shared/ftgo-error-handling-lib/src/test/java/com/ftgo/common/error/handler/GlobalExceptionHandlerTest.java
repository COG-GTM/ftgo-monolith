package com.ftgo.common.error.handler;

import com.ftgo.common.error.code.CommonErrorCode;
import com.ftgo.common.error.code.OrderErrorCode;
import com.ftgo.common.error.exception.BusinessRuleException;
import com.ftgo.common.error.exception.ResourceNotFoundException;
import com.ftgo.common.error.exception.ServiceCommunicationException;
import com.ftgo.common.error.exception.ServiceTimeoutException;
import com.ftgo.common.error.exception.StateTransitionException;
import com.ftgo.common.error.config.FtgoErrorHandlingAutoConfiguration;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for {@link GlobalExceptionHandler}.
 *
 * <p>Uses a test controller to trigger different exception types
 * and verifies the standardized error response format.</p>
 */
@WebMvcTest(controllers = GlobalExceptionHandlerTest.TestController.class)
@ImportAutoConfiguration(FtgoErrorHandlingAutoConfiguration.class)
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    // =========================================================================
    // Test Controller
    // =========================================================================

    @RestController
    @RequestMapping("/test")
    static class TestController {

        @GetMapping("/resource-not-found")
        public void resourceNotFound() {
            throw new ResourceNotFoundException("Order", 123L);
        }

        @GetMapping("/business-rule")
        public void businessRule() {
            throw new BusinessRuleException(OrderErrorCode.ORDER_MINIMUM_NOT_MET,
                    "Order total must be at least $10.00");
        }

        @GetMapping("/state-transition")
        public void stateTransition() {
            throw new StateTransitionException(TestState.APPROVED);
        }

        @GetMapping("/service-communication")
        public void serviceCommunication() {
            throw new ServiceCommunicationException("restaurant-service",
                    new RuntimeException("Connection refused"));
        }

        @GetMapping("/service-timeout")
        public void serviceTimeout() {
            throw new ServiceTimeoutException("courier-service");
        }

        @GetMapping("/internal-error")
        public void internalError() {
            throw new RuntimeException("Something went wrong");
        }

        @PostMapping("/validate")
        public void validate(@Valid @RequestBody TestRequest request) {
            // Validation handled by Bean Validation
        }

        @GetMapping("/orders/{id}")
        public void getOrder(@PathVariable Long id) {
            throw new ResourceNotFoundException("Order", id);
        }
    }

    enum TestState {
        APPROVED, DELIVERED
    }

    static class TestRequest {
        @NotBlank(message = "Name is required")
        private String name;

        @NotNull(message = "Amount is required")
        @Min(value = 1, message = "Amount must be at least 1")
        private Integer amount;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public Integer getAmount() { return amount; }
        public void setAmount(Integer amount) { this.amount = amount; }
    }

    // =========================================================================
    // Tests: FTGO Custom Exceptions
    // =========================================================================

    @Nested
    @DisplayName("FTGO Custom Exceptions")
    class FtgoExceptions {

        @Test
        @DisplayName("ResourceNotFoundException returns 404 with error details")
        void resourceNotFound() throws Exception {
            mockMvc.perform(get("/test/resource-not-found"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error.code", is("RESOURCE_NOT_FOUND")))
                    .andExpect(jsonPath("$.error.message", is("Order with id '123' was not found")))
                    .andExpect(jsonPath("$.error.status", is(404)))
                    .andExpect(jsonPath("$.error.timestamp", notNullValue()))
                    .andExpect(jsonPath("$.error.instance", is("/test/resource-not-found")));
        }

        @Test
        @DisplayName("BusinessRuleException returns 422 with domain error code")
        void businessRule() throws Exception {
            mockMvc.perform(get("/test/business-rule"))
                    .andExpect(status().isUnprocessableEntity())
                    .andExpect(jsonPath("$.error.code", is("ORDER_MINIMUM_NOT_MET")))
                    .andExpect(jsonPath("$.error.message", is("Order total must be at least $10.00")))
                    .andExpect(jsonPath("$.error.status", is(422)));
        }

        @Test
        @DisplayName("StateTransitionException returns 409 Conflict")
        void stateTransition() throws Exception {
            mockMvc.perform(get("/test/state-transition"))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.error.code", is("STATE_TRANSITION_ERROR")))
                    .andExpect(jsonPath("$.error.message",
                            is("Invalid state transition from current state: APPROVED")))
                    .andExpect(jsonPath("$.error.status", is(409)));
        }

        @Test
        @DisplayName("ServiceCommunicationException returns 502 Bad Gateway")
        void serviceCommunication() throws Exception {
            mockMvc.perform(get("/test/service-communication"))
                    .andExpect(status().isBadGateway())
                    .andExpect(jsonPath("$.error.code", is("UPSTREAM_SERVICE_ERROR")))
                    .andExpect(jsonPath("$.error.status", is(502)));
        }

        @Test
        @DisplayName("ServiceTimeoutException returns 504 Gateway Timeout")
        void serviceTimeout() throws Exception {
            mockMvc.perform(get("/test/service-timeout"))
                    .andExpect(status().isGatewayTimeout())
                    .andExpect(jsonPath("$.error.code", is("SERVICE_TIMEOUT")))
                    .andExpect(jsonPath("$.error.status", is(504)));
        }
    }

    // =========================================================================
    // Tests: Validation Exceptions
    // =========================================================================

    @Nested
    @DisplayName("Validation Exceptions")
    class ValidationExceptions {

        @Test
        @DisplayName("Validation errors return 400 with field-level details")
        void validationErrors() throws Exception {
            mockMvc.perform(post("/test/validate")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"name\": \"\", \"amount\": 0}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error.code", is("VALIDATION_ERROR")))
                    .andExpect(jsonPath("$.error.status", is(400)))
                    .andExpect(jsonPath("$.error.details", hasSize(2)));
        }

        @Test
        @DisplayName("Missing required fields return 400 with details")
        void missingFields() throws Exception {
            mockMvc.perform(post("/test/validate")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error.code", is("VALIDATION_ERROR")))
                    .andExpect(jsonPath("$.error.details").isArray());
        }

        @Test
        @DisplayName("Malformed JSON returns 400")
        void malformedJson() throws Exception {
            mockMvc.perform(post("/test/validate")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{invalid json}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error.code", is("INVALID_REQUEST")))
                    .andExpect(jsonPath("$.error.message", is("Malformed JSON request body")));
        }
    }

    // =========================================================================
    // Tests: Spring MVC Exceptions
    // =========================================================================

    @Nested
    @DisplayName("Spring MVC Exceptions")
    class SpringMvcExceptions {

        @Test
        @DisplayName("Method not allowed returns 405")
        void methodNotAllowed() throws Exception {
            mockMvc.perform(delete("/test/resource-not-found"))
                    .andExpect(status().isMethodNotAllowed())
                    .andExpect(jsonPath("$.error.code", is("METHOD_NOT_ALLOWED")));
        }

        @Test
        @DisplayName("Unsupported media type returns 415")
        void unsupportedMediaType() throws Exception {
            mockMvc.perform(post("/test/validate")
                            .contentType(MediaType.APPLICATION_XML)
                            .content("<request/>"))
                    .andExpect(status().isUnsupportedMediaType())
                    .andExpect(jsonPath("$.error.code", is("UNSUPPORTED_MEDIA_TYPE")));
        }
    }

    // =========================================================================
    // Tests: Catch-all Handler
    // =========================================================================

    @Nested
    @DisplayName("Catch-all Handler")
    class CatchAllHandler {

        @Test
        @DisplayName("Unhandled exceptions return 500 without stack trace")
        void unhandledException() throws Exception {
            mockMvc.perform(get("/test/internal-error"))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.error.code", is("INTERNAL_ERROR")))
                    .andExpect(jsonPath("$.error.message",
                            is("An unexpected internal error occurred")))
                    .andExpect(jsonPath("$.error.status", is(500)))
                    .andExpect(jsonPath("$.error.timestamp", notNullValue()));
        }
    }

    // =========================================================================
    // Tests: Error Response Format
    // =========================================================================

    @Nested
    @DisplayName("Error Response Format")
    class ErrorResponseFormat {

        @Test
        @DisplayName("All error responses include standard fields")
        void standardFields() throws Exception {
            mockMvc.perform(get("/test/resource-not-found"))
                    .andExpect(jsonPath("$.error.code", notNullValue()))
                    .andExpect(jsonPath("$.error.message", notNullValue()))
                    .andExpect(jsonPath("$.error.status", notNullValue()))
                    .andExpect(jsonPath("$.error.timestamp", notNullValue()))
                    .andExpect(jsonPath("$.error.instance", notNullValue()));
        }

        @Test
        @DisplayName("Error response is wrapped under 'error' key")
        void wrappedUnderErrorKey() throws Exception {
            mockMvc.perform(get("/test/resource-not-found"))
                    .andExpect(jsonPath("$.error").exists())
                    .andExpect(jsonPath("$.error.code").exists());
        }
    }
}
