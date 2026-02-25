package com.ftgo.common.error.response;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link ErrorResponse} and related classes.
 */
class ErrorResponseTest {

    @Test
    @DisplayName("Builder creates ErrorResponse with all fields")
    void builderCreatesFullResponse() {
        Instant now = Instant.now();
        List<ErrorDetail> details = List.of(
                ErrorDetail.of("name", "must not be blank", ""),
                ErrorDetail.of("amount", "must be at least 1", 0)
        );

        ErrorResponse response = ErrorResponse.builder()
                .code("VALIDATION_ERROR")
                .message("Request validation failed")
                .status(400)
                .details(details)
                .timestamp(now)
                .traceId("abc123")
                .instance("/api/orders")
                .build();

        assertThat(response.getCode()).isEqualTo("VALIDATION_ERROR");
        assertThat(response.getMessage()).isEqualTo("Request validation failed");
        assertThat(response.getStatus()).isEqualTo(400);
        assertThat(response.getDetails()).hasSize(2);
        assertThat(response.getTimestamp()).isEqualTo(now);
        assertThat(response.getTraceId()).isEqualTo("abc123");
        assertThat(response.getInstance()).isEqualTo("/api/orders");
    }

    @Test
    @DisplayName("Builder creates ErrorResponse with minimal fields")
    void builderCreatesMinimalResponse() {
        ErrorResponse response = ErrorResponse.builder()
                .code("INTERNAL_ERROR")
                .message("An unexpected error occurred")
                .status(500)
                .build();

        assertThat(response.getCode()).isEqualTo("INTERNAL_ERROR");
        assertThat(response.getMessage()).isEqualTo("An unexpected error occurred");
        assertThat(response.getStatus()).isEqualTo(500);
        assertThat(response.getDetails()).isNull();
        assertThat(response.getTraceId()).isNull();
        assertThat(response.getInstance()).isNull();
        assertThat(response.getTimestamp()).isNotNull(); // default from builder
    }

    @Test
    @DisplayName("ErrorDetail.of creates detail with all fields")
    void errorDetailWithAllFields() {
        ErrorDetail detail = ErrorDetail.of("email", "must not be blank", "");

        assertThat(detail.getField()).isEqualTo("email");
        assertThat(detail.getMessage()).isEqualTo("must not be blank");
        assertThat(detail.getRejectedValue()).isEqualTo("");
    }

    @Test
    @DisplayName("ErrorDetail.of creates detail without rejected value")
    void errorDetailWithoutRejectedValue() {
        ErrorDetail detail = ErrorDetail.of("email", "must not be blank");

        assertThat(detail.getField()).isEqualTo("email");
        assertThat(detail.getMessage()).isEqualTo("must not be blank");
        assertThat(detail.getRejectedValue()).isNull();
    }

    @Test
    @DisplayName("ErrorResponseWrapper wraps error correctly")
    void wrapperWrapsError() {
        ErrorResponse error = ErrorResponse.builder()
                .code("TEST")
                .message("test")
                .status(400)
                .build();

        ErrorResponseWrapper wrapper = ErrorResponseWrapper.of(error);

        assertThat(wrapper.getError()).isSameAs(error);
        assertThat(wrapper.getError().getCode()).isEqualTo("TEST");
    }
}
