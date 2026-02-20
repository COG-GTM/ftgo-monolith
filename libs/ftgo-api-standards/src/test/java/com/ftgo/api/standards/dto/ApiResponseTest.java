package com.ftgo.api.standards.dto;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ApiResponseTest {

    @Test
    void successWithData() {
        ApiResponse<String> response = ApiResponse.success("test-data");

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getData()).isEqualTo("test-data");
        assertThat(response.getMessage()).isNull();
        assertThat(response.getPath()).isNull();
        assertThat(response.getTimestamp()).isNotNull();
    }

    @Test
    void successWithDataAndMessage() {
        ApiResponse<String> response = ApiResponse.success("data", "Created successfully");

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getData()).isEqualTo("data");
        assertThat(response.getMessage()).isEqualTo("Created successfully");
    }

    @Test
    void successWithDataMessageAndPath() {
        ApiResponse<String> response = ApiResponse.success("data", "OK", "/api/v1/orders");

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getPath()).isEqualTo("/api/v1/orders");
    }

    @Test
    void errorWithMessage() {
        ApiResponse<Object> response = ApiResponse.error("Something went wrong");

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getData()).isNull();
        assertThat(response.getMessage()).isEqualTo("Something went wrong");
        assertThat(response.getTimestamp()).isNotNull();
    }

    @Test
    void errorWithMessageAndPath() {
        ApiResponse<Object> response = ApiResponse.error("Not found", "/api/v1/orders/999");

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getPath()).isEqualTo("/api/v1/orders/999");
    }
}
