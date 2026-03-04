package net.chrisrichardson.ftgo.errorhandling;

import io.micrometer.tracing.Tracer;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import net.chrisrichardson.ftgo.common.NotYetImplementedException;
import net.chrisrichardson.ftgo.common.UnsupportedStateTransitionException;
import net.chrisrichardson.ftgo.domain.OrderMinimumNotMetException;
import net.chrisrichardson.ftgo.errorhandling.config.GlobalExceptionHandler;
import net.chrisrichardson.ftgo.errorhandling.constants.FtgoErrorCodes;
import net.chrisrichardson.ftgo.errorhandling.exception.ResourceNotFoundException;
import net.chrisrichardson.ftgo.errorhandling.exception.ServiceCommunicationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for the {@link GlobalExceptionHandler}.
 *
 * <p>Uses a test controller that throws various exceptions to verify
 * the centralized handler produces correct HTTP statuses and standardized
 * {@link net.chrisrichardson.ftgo.errorhandling.model.FtgoErrorResponse} bodies.</p>
 */
@WebMvcTest(excludeAutoConfiguration = SecurityAutoConfiguration.class)
@ContextConfiguration(classes = {
        GlobalExceptionHandlerTest.TestConfig.class,
        GlobalExceptionHandlerTest.TestController.class
})
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @Configuration
    static class TestConfig {
        @Bean
        public Tracer tracer() {
            return Tracer.NOOP;
        }

        @Bean
        public GlobalExceptionHandler globalExceptionHandler(Tracer tracer) {
            return new GlobalExceptionHandler(tracer);
        }
    }

    // =========================================================================
    // Domain / Business Exception Tests
    // =========================================================================

    @Test
    void shouldReturn409ForUnsupportedStateTransition() throws Exception {
        mockMvc.perform(get("/test/unsupported-state"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status", is("error")))
                .andExpect(jsonPath("$.code", is(FtgoErrorCodes.UNSUPPORTED_STATE_TRANSITION)))
                .andExpect(jsonPath("$.message", notNullValue()))
                .andExpect(jsonPath("$.path", is("/test/unsupported-state")))
                .andExpect(jsonPath("$.timestamp", notNullValue()));
    }

    @Test
    void shouldReturn422ForOrderMinimumNotMet() throws Exception {
        mockMvc.perform(get("/test/order-minimum-not-met"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.status", is("error")))
                .andExpect(jsonPath("$.code", is(FtgoErrorCodes.ORDER_MINIMUM_NOT_MET)))
                .andExpect(jsonPath("$.message", is("Order total does not meet the restaurant minimum")))
                .andExpect(jsonPath("$.path", is("/test/order-minimum-not-met")))
                .andExpect(jsonPath("$.timestamp", notNullValue()));
    }

    @Test
    void shouldReturn501ForNotYetImplemented() throws Exception {
        mockMvc.perform(get("/test/not-implemented"))
                .andExpect(status().isNotImplemented())
                .andExpect(jsonPath("$.status", is("error")))
                .andExpect(jsonPath("$.code", is(FtgoErrorCodes.NOT_IMPLEMENTED)))
                .andExpect(jsonPath("$.message", is("This functionality is not yet implemented")))
                .andExpect(jsonPath("$.timestamp", notNullValue()));
    }

    @Test
    void shouldReturn404ForResourceNotFound() throws Exception {
        mockMvc.perform(get("/test/resource-not-found"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is("error")))
                .andExpect(jsonPath("$.code", is(FtgoErrorCodes.RESOURCE_NOT_FOUND)))
                .andExpect(jsonPath("$.message", is("Order with id 123 not found")))
                .andExpect(jsonPath("$.timestamp", notNullValue()));
    }

    // =========================================================================
    // Bean Validation Tests
    // =========================================================================

    @Test
    void shouldReturn400ForBeanValidationFailures() throws Exception {
        String invalidBody = "{\"name\":\"\",\"quantity\":-1}";

        mockMvc.perform(post("/test/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is("error")))
                .andExpect(jsonPath("$.code", is(FtgoErrorCodes.VALIDATION_FAILED)))
                .andExpect(jsonPath("$.message", is("Validation failed")))
                .andExpect(jsonPath("$.details", hasSize(2)))
                .andExpect(jsonPath("$.timestamp", notNullValue()));
    }

    // =========================================================================
    // HTTP / Spring MVC Exception Tests
    // =========================================================================

    @Test
    void shouldReturn400ForMalformedJson() throws Exception {
        mockMvc.perform(post("/test/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{invalid json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is("error")))
                .andExpect(jsonPath("$.code", is(FtgoErrorCodes.BAD_REQUEST)))
                .andExpect(jsonPath("$.message", is("Malformed request body")))
                .andExpect(jsonPath("$.timestamp", notNullValue()));
    }

    @Test
    void shouldReturn405ForMethodNotAllowed() throws Exception {
        mockMvc.perform(delete("/test/unsupported-state"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.status", is("error")))
                .andExpect(jsonPath("$.code", is(FtgoErrorCodes.METHOD_NOT_ALLOWED)))
                .andExpect(jsonPath("$.timestamp", notNullValue()));
    }

    // =========================================================================
    // Security Exception Tests
    // =========================================================================

    @Test
    void shouldReturn403ForAccessDenied() throws Exception {
        mockMvc.perform(get("/test/access-denied"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status", is("error")))
                .andExpect(jsonPath("$.code", is(FtgoErrorCodes.ACCESS_DENIED)))
                .andExpect(jsonPath("$.message", is("Access denied")))
                .andExpect(jsonPath("$.timestamp", notNullValue()));
    }

    @Test
    void shouldReturn401ForAuthenticationException() throws Exception {
        mockMvc.perform(get("/test/auth-failure"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status", is("error")))
                .andExpect(jsonPath("$.code", is(FtgoErrorCodes.UNAUTHORIZED)))
                .andExpect(jsonPath("$.message", is("Authentication required")))
                .andExpect(jsonPath("$.timestamp", notNullValue()));
    }

    // =========================================================================
    // Inter-Service Communication Exception Tests
    // =========================================================================

    @Test
    void shouldReturn502ForServiceCommunicationError() throws Exception {
        mockMvc.perform(get("/test/service-error"))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.status", is("error")))
                .andExpect(jsonPath("$.code", is(FtgoErrorCodes.SERVICE_COMMUNICATION_ERROR)))
                .andExpect(jsonPath("$.timestamp", notNullValue()));
    }

    @Test
    void shouldReturn504ForServiceTimeout() throws Exception {
        mockMvc.perform(get("/test/service-timeout"))
                .andExpect(status().isGatewayTimeout())
                .andExpect(jsonPath("$.status", is("error")))
                .andExpect(jsonPath("$.code", is(FtgoErrorCodes.SERVICE_TIMEOUT)))
                .andExpect(jsonPath("$.timestamp", notNullValue()));
    }

    // =========================================================================
    // Catch-All Exception Test
    // =========================================================================

    @Test
    void shouldReturn500ForUnhandledExceptionWithoutStackTrace() throws Exception {
        mockMvc.perform(get("/test/generic-error"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status", is("error")))
                .andExpect(jsonPath("$.code", is(FtgoErrorCodes.INTERNAL_ERROR)))
                .andExpect(jsonPath("$.message", is("An unexpected error occurred")))
                .andExpect(jsonPath("$.timestamp", notNullValue()))
                // Verify no stack trace is leaked
                .andExpect(jsonPath("$.stackTrace").doesNotExist())
                .andExpect(jsonPath("$.exception").doesNotExist())
                .andExpect(jsonPath("$.trace").doesNotExist());
    }

    // =========================================================================
    // Error Response Format Tests
    // =========================================================================

    @Test
    void shouldIncludeAllRequiredFieldsInErrorResponse() throws Exception {
        mockMvc.perform(get("/test/resource-not-found"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").exists())
                .andExpect(jsonPath("$.code").exists())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.path").exists())
                .andExpect(jsonPath("$.timestamp").exists())
                // details should be null (not a validation error)
                .andExpect(jsonPath("$.details").doesNotExist());
    }

    @Test
    void shouldNotLeakInternalDetailsIn500Response() throws Exception {
        mockMvc.perform(get("/test/generic-error"))
                .andExpect(status().isInternalServerError())
                // Must NOT contain any of these internal details
                .andExpect(jsonPath("$.stackTrace").doesNotExist())
                .andExpect(jsonPath("$.exception").doesNotExist())
                .andExpect(jsonPath("$.trace").doesNotExist())
                .andExpect(jsonPath("$.cause").doesNotExist())
                // Message should be generic, not the exception's internal message
                .andExpect(jsonPath("$.message", is("An unexpected error occurred")));
    }

    // =========================================================================
    // Test Controller — throws exceptions for testing
    // =========================================================================

    @RestController
    static class TestController {

        @GetMapping("/test/unsupported-state")
        public void unsupportedState() {
            throw new UnsupportedStateTransitionException(TestState.APPROVED);
        }

        @GetMapping("/test/order-minimum-not-met")
        public void orderMinimumNotMet() {
            throw new OrderMinimumNotMetException();
        }

        @GetMapping("/test/not-implemented")
        public void notImplemented() {
            throw new NotYetImplementedException();
        }

        @GetMapping("/test/resource-not-found")
        public void resourceNotFound() {
            throw new ResourceNotFoundException("Order", "123");
        }

        @PostMapping("/test/validate")
        public void validate(@Valid @RequestBody TestDto dto) {
            // If validation passes, nothing happens
        }

        @GetMapping("/test/access-denied")
        public void accessDenied() {
            throw new AccessDeniedException("Insufficient permissions");
        }

        @GetMapping("/test/auth-failure")
        public void authFailure() {
            throw new BadCredentialsException("Invalid credentials");
        }

        @GetMapping("/test/service-error")
        public void serviceError() {
            throw new ServiceCommunicationException("consumer-service", "Connection refused");
        }

        @GetMapping("/test/service-timeout")
        public void serviceTimeout() {
            throw new ServiceCommunicationException(
                    "restaurant-service", "Read timed out", new RuntimeException("timeout"), true);
        }

        @GetMapping("/test/generic-error")
        public void genericError() {
            throw new RuntimeException("This internal message should NOT be leaked");
        }

        enum TestState {
            APPROVED
        }
    }

    /**
     * Test DTO with Bean Validation constraints.
     */
    static class TestDto {
        @NotBlank(message = "Name is required")
        private String name;

        @NotNull(message = "Quantity is required")
        @Min(value = 1, message = "Quantity must be at least 1")
        private Integer quantity;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Integer getQuantity() {
            return quantity;
        }

        public void setQuantity(Integer quantity) {
            this.quantity = quantity;
        }
    }
}
