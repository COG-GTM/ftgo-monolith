package com.ftgo.errorhandling.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.ftgo.errorhandling.constants.ErrorCodes;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for {@link ErrorResponse} DTO serialization and construction.
 */
public class ErrorResponseTest {

    private ObjectMapper objectMapper;

    @Before
    public void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    public void shouldCreateErrorResponseWithRequiredFields() {
        ErrorResponse response = new ErrorResponse(
                ErrorCodes.INTERNAL_ERROR, "Internal server error", 500);

        assertEquals(ErrorCodes.INTERNAL_ERROR, response.getCode());
        assertEquals("Internal server error", response.getMessage());
        assertEquals(500, response.getStatus());
        assertNotNull(response.getTimestamp());
        assertNull(response.getDetails());
        assertTrue(response.getFieldErrors().isEmpty());
    }

    @Test
    public void shouldCreateErrorResponseWithAllFields() {
        ErrorResponse response = new ErrorResponse(
                ErrorCodes.VALIDATION_FAILED, "Validation failed", "2 field(s) have errors", 400);
        response.setTraceId("abc123");
        response.setPath("/orders");

        assertEquals(ErrorCodes.VALIDATION_FAILED, response.getCode());
        assertEquals("Validation failed", response.getMessage());
        assertEquals("2 field(s) have errors", response.getDetails());
        assertEquals(400, response.getStatus());
        assertEquals("abc123", response.getTraceId());
        assertEquals("/orders", response.getPath());
    }

    @Test
    public void shouldAddFieldErrors() {
        ErrorResponse response = new ErrorResponse(
                ErrorCodes.VALIDATION_FAILED, "Validation failed", 400);
        response.addFieldError("name", "must not be null", null);
        response.addFieldError("quantity", "must be at least 1", -1);

        assertEquals(2, response.getFieldErrors().size());
        assertEquals("name", response.getFieldErrors().get(0).getField());
        assertEquals("must not be null", response.getFieldErrors().get(0).getMessage());
        assertNull(response.getFieldErrors().get(0).getRejectedValue());
        assertEquals("quantity", response.getFieldErrors().get(1).getField());
        assertEquals("must be at least 1", response.getFieldErrors().get(1).getMessage());
        assertEquals(-1, response.getFieldErrors().get(1).getRejectedValue());
    }

    @Test
    public void shouldSupportFluentBuilderPattern() {
        ErrorResponse response = new ErrorResponse(
                ErrorCodes.RESOURCE_NOT_FOUND, "Not found", 404)
                .withDetails("Order not found with id: 42")
                .withTraceId("trace-123")
                .withPath("/orders/42")
                .withFieldError("orderId", "not found", 42);

        assertEquals("Order not found with id: 42", response.getDetails());
        assertEquals("trace-123", response.getTraceId());
        assertEquals("/orders/42", response.getPath());
        assertEquals(1, response.getFieldErrors().size());
    }

    @Test
    public void shouldSerializeToJsonWithAllFields() throws Exception {
        ErrorResponse response = new ErrorResponse(
                ErrorCodes.VALIDATION_FAILED, "Validation failed", "1 field(s) have errors", 400);
        response.setTraceId("abc123");
        response.setPath("/orders");
        response.addFieldError("consumerId", "must not be null", null);

        String json = objectMapper.writeValueAsString(response);

        assertTrue(json.contains("\"code\":\"FTGO-01001\""));
        assertTrue(json.contains("\"message\":\"Validation failed\""));
        assertTrue(json.contains("\"details\":\"1 field(s) have errors\""));
        assertTrue(json.contains("\"status\":400"));
        assertTrue(json.contains("\"traceId\":\"abc123\""));
        assertTrue(json.contains("\"path\":\"/orders\""));
        assertTrue(json.contains("\"fieldErrors\""));
        assertTrue(json.contains("\"field\":\"consumerId\""));
        assertTrue(json.contains("\"message\":\"must not be null\""));
    }

    @Test
    public void shouldOmitNullFieldsInJson() throws Exception {
        ErrorResponse response = new ErrorResponse(
                ErrorCodes.INTERNAL_ERROR, "Internal server error", 500);

        String json = objectMapper.writeValueAsString(response);

        assertFalse("Should not contain details when null", json.contains("\"details\""));
        assertFalse("Should not contain traceId when null", json.contains("\"traceId\""));
        assertFalse("Should not contain path when null", json.contains("\"path\""));
    }

    @Test
    public void shouldOmitEmptyFieldErrorsInJson() throws Exception {
        ErrorResponse response = new ErrorResponse(
                ErrorCodes.INTERNAL_ERROR, "Internal server error", 500);

        String json = objectMapper.writeValueAsString(response);

        assertFalse("Should not contain fieldErrors when empty", json.contains("\"fieldErrors\""));
    }

    @Test
    public void shouldDeserializeFromJson() throws Exception {
        String json = "{\"code\":\"FTGO-00001\",\"message\":\"Internal server error\"," +
                "\"status\":500,\"timestamp\":\"2026-03-03T00:00:00Z\"}";

        ErrorResponse response = objectMapper.readValue(json, ErrorResponse.class);

        assertEquals(ErrorCodes.INTERNAL_ERROR, response.getCode());
        assertEquals("Internal server error", response.getMessage());
        assertEquals(500, response.getStatus());
    }

    @Test
    public void shouldReturnUnmodifiableFieldErrorsList() {
        ErrorResponse response = new ErrorResponse(
                ErrorCodes.VALIDATION_FAILED, "Validation failed", 400);
        response.addFieldError("name", "required", null);

        try {
            response.getFieldErrors().add(new ErrorResponse.FieldError("test", "test", null));
            fail("Should throw UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // Expected - list should be unmodifiable
        }
    }

    @Test
    public void shouldCreateFieldError() {
        ErrorResponse.FieldError fieldError = new ErrorResponse.FieldError(
                "email", "must be a valid email address", "not-an-email");

        assertEquals("email", fieldError.getField());
        assertEquals("must be a valid email address", fieldError.getMessage());
        assertEquals("not-an-email", fieldError.getRejectedValue());
    }
}
