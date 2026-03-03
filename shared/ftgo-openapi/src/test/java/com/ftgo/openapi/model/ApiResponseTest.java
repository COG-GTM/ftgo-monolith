package com.ftgo.openapi.model;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * Unit tests for the standard API response models.
 */
public class ApiResponseTest {

    @Test
    public void testSuccessResponse() {
        ApiResponse<String> response = ApiResponse.success("test data");

        assertEquals("success", response.getStatus());
        assertEquals("test data", response.getData());
        assertNull(response.getError());
        assertNotNull(response.getTimestamp());
    }

    @Test
    public void testErrorResponse() {
        ApiResponse<Void> response = ApiResponse.error("NOT_FOUND", "Resource not found");

        assertEquals("error", response.getStatus());
        assertNull(response.getData());
        assertNotNull(response.getError());
        assertEquals("NOT_FOUND", response.getError().getCode());
        assertEquals("Resource not found", response.getError().getMessage());
        assertNotNull(response.getTimestamp());
    }

    @Test
    public void testErrorResponseWithDetails() {
        ApiResponse<Void> response = ApiResponse.error(
                "VALIDATION_FAILED",
                "Validation failed",
                "Field 'name' must not be blank"
        );

        assertEquals("error", response.getStatus());
        assertNotNull(response.getError());
        assertEquals("VALIDATION_FAILED", response.getError().getCode());
        assertEquals("Validation failed", response.getError().getMessage());
        assertEquals("Field 'name' must not be blank", response.getError().getDetails());
    }

    @Test
    public void testPagedResponse() {
        List<String> items = Arrays.asList("item1", "item2", "item3");
        PagedResponse<String> response = PagedResponse.of(items, 0, 20, 42);

        assertEquals("success", response.getStatus());
        assertEquals(3, response.getData().size());
        assertNotNull(response.getPagination());
        assertEquals(0, response.getPagination().getPage());
        assertEquals(20, response.getPagination().getSize());
        assertEquals(42L, response.getPagination().getTotalElements());
        assertEquals(3L, response.getPagination().getTotalPages());
        assertEquals(true, response.getPagination().isHasNext());
        assertEquals(false, response.getPagination().isHasPrevious());
    }

    @Test
    public void testPagedResponseLastPage() {
        List<String> items = Arrays.asList("item1", "item2");
        PagedResponse<String> response = PagedResponse.of(items, 2, 20, 42);

        assertEquals(2, response.getPagination().getPage());
        assertEquals(false, response.getPagination().isHasNext());
        assertEquals(true, response.getPagination().isHasPrevious());
    }

    @Test
    public void testApiErrorWithFieldErrors() {
        ApiError error = new ApiError("VALIDATION_FAILED", "Validation failed");
        ApiError.FieldError fieldError = new ApiError.FieldError("name", "must not be blank", "");
        error.setFieldErrors(Arrays.asList(fieldError));

        assertEquals("VALIDATION_FAILED", error.getCode());
        assertEquals(1, error.getFieldErrors().size());
        assertEquals("name", error.getFieldErrors().get(0).getField());
        assertEquals("must not be blank", error.getFieldErrors().get(0).getMessage());
    }
}
