package com.ftgo.errorhandling.handler;

import com.ftgo.errorhandling.config.FtgoErrorHandlingAutoConfiguration;
import com.ftgo.errorhandling.constants.ErrorCodes;
import com.ftgo.errorhandling.exception.BusinessRuleException;
import com.ftgo.errorhandling.exception.ConflictException;
import com.ftgo.errorhandling.exception.ResourceNotFoundException;
import com.ftgo.errorhandling.exception.ServiceCommunicationException;
import net.chrisrichardson.ftgo.common.UnsupportedStateTransitionException;
import net.chrisrichardson.ftgo.domain.OrderMinimumNotMetException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for {@link GlobalExceptionHandler}.
 *
 * <p>Uses a minimal Spring MVC test context with a test controller that
 * deliberately throws various exceptions to verify the error handling behavior.</p>
 */
@RunWith(SpringRunner.class)
@WebMvcTest(controllers = GlobalExceptionHandlerTest.TestController.class)
@ImportAutoConfiguration(FtgoErrorHandlingAutoConfiguration.class)
public class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    // -------------------------------------------------------------------------
    // Validation Error Tests (400)
    // -------------------------------------------------------------------------

    @Test
    public void shouldReturn400WithFieldErrorsForInvalidRequestBody() throws Exception {
        String invalidJson = "{\"name\": \"\", \"quantity\": -1}";

        mockMvc.perform(post("/test/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCodes.VALIDATION_FAILED))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.timestamp").isNotEmpty())
                .andExpect(jsonPath("$.traceId").isNotEmpty())
                .andExpect(jsonPath("$.path").value("/test/validate"))
                .andExpect(jsonPath("$.fieldErrors").isArray())
                .andExpect(jsonPath("$.fieldErrors", hasSize(greaterThanOrEqualTo(2))));
    }

    @Test
    public void shouldReturn400ForMalformedJson() throws Exception {
        mockMvc.perform(post("/test/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{invalid json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCodes.MALFORMED_REQUEST))
                .andExpect(jsonPath("$.message").value("Malformed request body"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.timestamp").isNotEmpty())
                .andExpect(jsonPath("$.traceId").isNotEmpty());
    }

    // -------------------------------------------------------------------------
    // Not Found Error Tests (404)
    // -------------------------------------------------------------------------

    @Test
    public void shouldReturn404ForResourceNotFound() throws Exception {
        mockMvc.perform(get("/test/not-found"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(ErrorCodes.RESOURCE_NOT_FOUND))
                .andExpect(jsonPath("$.message").value("Resource not found"))
                .andExpect(jsonPath("$.details").value("Order not found with id: 42"))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.timestamp").isNotEmpty())
                .andExpect(jsonPath("$.traceId").isNotEmpty())
                .andExpect(jsonPath("$.path").value("/test/not-found"));
    }

    // -------------------------------------------------------------------------
    // Method Not Allowed Tests (405)
    // -------------------------------------------------------------------------

    @Test
    public void shouldReturn405ForUnsupportedMethod() throws Exception {
        mockMvc.perform(delete("/test/not-found"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.code").value(ErrorCodes.METHOD_NOT_ALLOWED))
                .andExpect(jsonPath("$.message").value("Method not allowed"))
                .andExpect(jsonPath("$.status").value(405));
    }

    // -------------------------------------------------------------------------
    // Conflict Error Tests (409)
    // -------------------------------------------------------------------------

    @Test
    public void shouldReturn409ForUnsupportedStateTransition() throws Exception {
        mockMvc.perform(get("/test/state-transition"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(ErrorCodes.ORDER_INVALID_STATE_TRANSITION))
                .andExpect(jsonPath("$.message").value("Invalid state transition"))
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.timestamp").isNotEmpty())
                .andExpect(jsonPath("$.traceId").isNotEmpty());
    }

    @Test
    public void shouldReturn409ForConflictException() throws Exception {
        mockMvc.perform(get("/test/conflict"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(ErrorCodes.ORDER_REVISION_CONFLICT))
                .andExpect(jsonPath("$.message").value("Conflict"))
                .andExpect(jsonPath("$.details").value("Order was modified by another request"))
                .andExpect(jsonPath("$.status").value(409));
    }

    // -------------------------------------------------------------------------
    // Business Rule Error Tests (422)
    // -------------------------------------------------------------------------

    @Test
    public void shouldReturn422ForOrderMinimumNotMet() throws Exception {
        mockMvc.perform(get("/test/order-minimum"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value(ErrorCodes.ORDER_MINIMUM_NOT_MET))
                .andExpect(jsonPath("$.message").value("Order minimum not met"))
                .andExpect(jsonPath("$.status").value(422))
                .andExpect(jsonPath("$.timestamp").isNotEmpty())
                .andExpect(jsonPath("$.traceId").isNotEmpty());
    }

    @Test
    public void shouldReturn422ForBusinessRuleException() throws Exception {
        mockMvc.perform(get("/test/business-rule"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value(ErrorCodes.CONSUMER_VALIDATION_FAILED))
                .andExpect(jsonPath("$.message").value("Business rule violation"))
                .andExpect(jsonPath("$.details").value("Consumer is not eligible for this order"))
                .andExpect(jsonPath("$.status").value(422));
    }

    // -------------------------------------------------------------------------
    // Service Communication Error Tests (502/503)
    // -------------------------------------------------------------------------

    @Test
    public void shouldReturn503ForServiceTimeout() throws Exception {
        mockMvc.perform(get("/test/service-timeout"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.code").value(ErrorCodes.SERVICE_TIMEOUT))
                .andExpect(jsonPath("$.message").value("Service communication failure"))
                .andExpect(jsonPath("$.status").value(503))
                .andExpect(jsonPath("$.timestamp").isNotEmpty())
                .andExpect(jsonPath("$.traceId").isNotEmpty());
    }

    @Test
    public void shouldReturn502ForDownstreamServiceError() throws Exception {
        mockMvc.perform(get("/test/service-error"))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.code").value(ErrorCodes.SERVICE_ERROR))
                .andExpect(jsonPath("$.message").value("Service communication failure"))
                .andExpect(jsonPath("$.status").value(502));
    }

    // -------------------------------------------------------------------------
    // Fallback Error Tests (500)
    // -------------------------------------------------------------------------

    @Test
    public void shouldReturn500ForUnhandledException() throws Exception {
        mockMvc.perform(get("/test/unexpected"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value(ErrorCodes.INTERNAL_ERROR))
                .andExpect(jsonPath("$.message").value("Internal server error"))
                .andExpect(jsonPath("$.details").value("An unexpected error occurred. Please try again later."))
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.timestamp").isNotEmpty())
                .andExpect(jsonPath("$.traceId").isNotEmpty())
                .andExpect(jsonPath("$.path").value("/test/unexpected"));
    }

    @Test
    public void shouldNotLeakStackTraceInErrorResponse() throws Exception {
        String response = mockMvc.perform(get("/test/unexpected"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Ensure no stack trace information leaks
        org.junit.Assert.assertFalse("Response should not contain stack trace",
                response.contains("at com.ftgo"));
        org.junit.Assert.assertFalse("Response should not contain 'NullPointerException'",
                response.contains("NullPointerException"));
    }

    // -------------------------------------------------------------------------
    // Response Format Tests
    // -------------------------------------------------------------------------

    @Test
    public void shouldIncludeAllRequiredFieldsInErrorResponse() throws Exception {
        mockMvc.perform(get("/test/not-found"))
                .andExpect(jsonPath("$.code").exists())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.details").exists())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.traceId").exists())
                .andExpect(jsonPath("$.path").exists())
                .andExpect(jsonPath("$.status").exists());
    }

    @Test
    public void shouldReturnJsonContentType() throws Exception {
        mockMvc.perform(get("/test/not-found"))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    // -------------------------------------------------------------------------
    // Test Controller and Configuration
    // -------------------------------------------------------------------------

    /**
     * Minimal test controller that throws various exceptions for testing.
     */
    @RestController
    static class TestController {

        @PostMapping("/test/validate")
        public String validate(@Valid @RequestBody TestRequest request) {
            return "ok";
        }

        @GetMapping("/test/not-found")
        public String notFound() {
            throw new ResourceNotFoundException("Order", 42L);
        }

        @GetMapping("/test/state-transition")
        public String stateTransition() {
            throw new UnsupportedStateTransitionException(TestState.APPROVED);
        }

        @GetMapping("/test/conflict")
        public String conflict() {
            throw new ConflictException(ErrorCodes.ORDER_REVISION_CONFLICT,
                    "Order was modified by another request");
        }

        @GetMapping("/test/order-minimum")
        public String orderMinimum() {
            throw new OrderMinimumNotMetException();
        }

        @GetMapping("/test/business-rule")
        public String businessRule() {
            throw new BusinessRuleException(ErrorCodes.CONSUMER_VALIDATION_FAILED,
                    "Consumer is not eligible for this order");
        }

        @GetMapping("/test/service-timeout")
        public String serviceTimeout() {
            throw new ServiceCommunicationException(ErrorCodes.SERVICE_TIMEOUT,
                    "consumer-service", "Request timed out after 5000ms");
        }

        @GetMapping("/test/service-error")
        public String serviceError() {
            throw new ServiceCommunicationException(ErrorCodes.SERVICE_ERROR,
                    "restaurant-service", "Downstream service returned 500");
        }

        @GetMapping("/test/unexpected")
        public String unexpected() {
            throw new RuntimeException("Something went terribly wrong");
        }
    }

    /**
     * Test request DTO with Bean Validation constraints.
     */
    static class TestRequest {

        @NotNull(message = "Name is required")
        @Size(min = 1, max = 100, message = "Name must be between 1 and 100 characters")
        private String name;

        @NotNull(message = "Quantity is required")
        @Min(value = 1, message = "Quantity must be at least 1")
        private Integer quantity;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }
    }

    /**
     * Test enum for state transition exceptions.
     */
    enum TestState {
        APPROVED, REJECTED
    }

}
