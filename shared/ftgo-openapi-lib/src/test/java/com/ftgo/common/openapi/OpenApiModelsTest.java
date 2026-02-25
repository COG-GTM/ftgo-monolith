package com.ftgo.common.openapi;

import com.ftgo.common.openapi.model.ApiErrorResponse;
import com.ftgo.common.openapi.model.ApiResponse;
import com.ftgo.common.openapi.model.PagedResponse;
import com.ftgo.common.openapi.model.SortDirection;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * Unit tests for the standard API response models.
 */
public class OpenApiModelsTest {

    @Test
    public void testApiResponseSuccess() {
        ApiResponse<String> response = ApiResponse.success("test-data");

        assertEquals("success", response.getStatus());
        assertEquals("test-data", response.getData());
        assertNotNull(response.getTimestamp());
        assertNull(response.getPath());
    }

    @Test
    public void testApiResponseSuccessWithPath() {
        ApiResponse<String> response = ApiResponse.success("test-data", "/api/v1/test");

        assertEquals("success", response.getStatus());
        assertEquals("test-data", response.getData());
        assertEquals("/api/v1/test", response.getPath());
        assertNotNull(response.getTimestamp());
    }

    @Test
    public void testApiResponseNoContent() {
        ApiResponse<Object> response = ApiResponse.noContent();

        assertEquals("success", response.getStatus());
        assertNull(response.getData());
    }

    @Test
    public void testApiErrorResponse() {
        ApiErrorResponse response = ApiErrorResponse.of("RESOURCE_NOT_FOUND", "Order not found", "/api/v1/orders/999");

        assertEquals("error", response.getStatus());
        assertEquals("RESOURCE_NOT_FOUND", response.getError().getCode());
        assertEquals("Order not found", response.getError().getMessage());
        assertEquals("/api/v1/orders/999", response.getPath());
        assertNotNull(response.getTimestamp());
    }

    @Test
    public void testApiErrorResponseWithDetails() {
        ApiErrorResponse response = ApiErrorResponse.of("VALIDATION_ERROR", "Validation failed", "/api/v1/orders")
                .addDetail("name", "must not be blank")
                .addDetail("quantity", "must be greater than 0");

        assertEquals("error", response.getStatus());
        assertEquals(2, response.getError().getDetails().size());
        assertEquals("name", response.getError().getDetails().get(0).getField());
        assertEquals("must not be blank", response.getError().getDetails().get(0).getMessage());
        assertEquals("quantity", response.getError().getDetails().get(1).getField());
    }

    @Test
    public void testPagedResponse() {
        List<String> items = List.of("item1", "item2", "item3");
        PagedResponse<String> response = PagedResponse.of(items, 0, 20, 150, 8);

        assertEquals("success", response.getStatus());
        assertEquals(3, response.getData().size());
        assertEquals(0, response.getPage().getNumber());
        assertEquals(20, response.getPage().getSize());
        assertEquals(150, response.getPage().getTotalElements());
        assertEquals(8, response.getPage().getTotalPages());
        assertNotNull(response.getTimestamp());
    }

    @Test
    public void testPagedResponseWithPath() {
        List<String> items = List.of("item1");
        PagedResponse<String> response = PagedResponse.of(items, 2, 10, 25, 3, "/api/v1/orders?page=2&size=10");

        assertEquals("/api/v1/orders?page=2&size=10", response.getPath());
        assertEquals(2, response.getPage().getNumber());
        assertEquals(10, response.getPage().getSize());
    }

    @Test
    public void testSortDirection() {
        assertEquals(SortDirection.ASC, SortDirection.valueOf("ASC"));
        assertEquals(SortDirection.DESC, SortDirection.valueOf("DESC"));
    }
}
