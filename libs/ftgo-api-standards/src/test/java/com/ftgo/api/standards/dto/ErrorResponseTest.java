package com.ftgo.api.standards.dto;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ErrorResponseTest {

    @Test
    void createErrorResponse() {
        ErrorResponse response = ErrorResponse.of(400, "VALIDATION_ERROR", "Invalid request");

        assertThat(response.getStatus()).isEqualTo(400);
        assertThat(response.getCode()).isEqualTo("VALIDATION_ERROR");
        assertThat(response.getMessage()).isEqualTo("Invalid request");
        assertThat(response.getPath()).isNull();
        assertThat(response.getTimestamp()).isNotNull();
        assertThat(response.getErrors()).isEmpty();
    }

    @Test
    void createErrorResponseWithPath() {
        ErrorResponse response = ErrorResponse.of(404, "NOT_FOUND", "Order not found", "/api/v1/orders/999");

        assertThat(response.getStatus()).isEqualTo(404);
        assertThat(response.getPath()).isEqualTo("/api/v1/orders/999");
    }

    @Test
    void addFieldErrors() {
        ErrorResponse response = ErrorResponse.of(400, "VALIDATION_ERROR", "Validation failed")
                .addFieldError("email", "must be a valid email")
                .addFieldError("name", "must not be blank");

        assertThat(response.getErrors()).hasSize(2);
        assertThat(response.getErrors().get(0).getField()).isEqualTo("email");
        assertThat(response.getErrors().get(0).getMessage()).isEqualTo("must be a valid email");
        assertThat(response.getErrors().get(1).getField()).isEqualTo("name");
    }
}
