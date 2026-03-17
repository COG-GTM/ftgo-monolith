package net.chrisrichardson.ftgo.openapi;

import net.chrisrichardson.ftgo.openapi.model.ApiErrorResponse;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ApiErrorResponseTest {

    @Test
    void shouldCreateErrorResponse() {
        ApiErrorResponse response = new ApiErrorResponse(404, "NOT_FOUND", "Order not found", "/orders/123");

        assertEquals(404, response.getStatus());
        assertEquals("NOT_FOUND", response.getError());
        assertEquals("Order not found", response.getMessage());
        assertEquals("/orders/123", response.getPath());
        assertNotNull(response.getTimestamp());
    }

    @Test
    void shouldIncludeFieldErrors() {
        ApiErrorResponse response = new ApiErrorResponse(400, "VALIDATION_FAILED", "Validation failed", "/orders");
        response.setFieldErrors(List.of(
                new ApiErrorResponse.FieldError("consumerId", null, "must not be null"),
                new ApiErrorResponse.FieldError("restaurantId", -1, "must be positive")
        ));

        assertNotNull(response.getFieldErrors());
        assertEquals(2, response.getFieldErrors().size());
        assertEquals("consumerId", response.getFieldErrors().get(0).getField());
        assertEquals("must not be null", response.getFieldErrors().get(0).getMessage());
    }
}
