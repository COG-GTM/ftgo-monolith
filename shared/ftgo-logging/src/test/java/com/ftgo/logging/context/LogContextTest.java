package com.ftgo.logging.context;

import com.ftgo.logging.LoggingConstants;
import org.junit.After;
import org.junit.Test;
import org.slf4j.MDC;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Unit tests for {@link LogContext}.
 * Verifies that MDC fields are correctly set and cleared.
 */
public class LogContextTest {

    @After
    public void tearDown() {
        MDC.clear();
    }

    @Test
    public void shouldSetAndGetUserId() {
        LogContext.setUserId("user-42");
        assertEquals("user-42", LogContext.getUserId());
        assertEquals("user-42", MDC.get(LoggingConstants.MDC_USER_ID));
    }

    @Test
    public void shouldSetAndGetCorrelationId() {
        LogContext.setCorrelationId("corr-123");
        assertEquals("corr-123", LogContext.getCorrelationId());
        assertEquals("corr-123", MDC.get(LoggingConstants.MDC_CORRELATION_ID));
    }

    @Test
    public void shouldSetAndGetRequestId() {
        LogContext.setRequestId("req-456");
        assertEquals("req-456", LogContext.getRequestId());
        assertEquals("req-456", MDC.get(LoggingConstants.MDC_REQUEST_ID));
    }

    @Test
    public void shouldSetAndGetTraceId() {
        LogContext.setTraceId("trace-789");
        assertEquals("trace-789", LogContext.getTraceId());
        assertEquals("trace-789", MDC.get(LoggingConstants.MDC_TRACE_ID));
    }

    @Test
    public void shouldSetAndGetSpanId() {
        LogContext.setSpanId("span-012");
        assertEquals("span-012", LogContext.getSpanId());
        assertEquals("span-012", MDC.get(LoggingConstants.MDC_SPAN_ID));
    }

    @Test
    public void shouldSetServiceName() {
        LogContext.setServiceName("order-service");
        assertEquals("order-service", MDC.get(LoggingConstants.MDC_SERVICE_NAME));
    }

    @Test
    public void shouldPutAndGetCustomField() {
        LogContext.put("orderId", "order-123");
        assertEquals("order-123", LogContext.get("orderId"));
    }

    @Test
    public void shouldRemoveSingleField() {
        LogContext.setUserId("user-1");
        LogContext.setRequestId("req-1");

        LogContext.remove(LoggingConstants.MDC_USER_ID);

        assertNull("userId should be removed", LogContext.getUserId());
        assertEquals("requestId should still exist", "req-1", LogContext.getRequestId());
    }

    @Test
    public void shouldClearAllFtgoFields() {
        LogContext.setUserId("user-1");
        LogContext.setRequestId("req-1");
        LogContext.setCorrelationId("corr-1");
        LogContext.setTraceId("trace-1");
        LogContext.setSpanId("span-1");
        LogContext.setServiceName("svc-1");

        // Add a custom field that should NOT be cleared by clear()
        LogContext.put("customField", "custom-value");

        LogContext.clear();

        assertNull("userId should be cleared", LogContext.getUserId());
        assertNull("requestId should be cleared", LogContext.getRequestId());
        assertNull("correlationId should be cleared", LogContext.getCorrelationId());
        assertNull("traceId should be cleared", LogContext.getTraceId());
        assertNull("spanId should be cleared", LogContext.getSpanId());
        assertNull("serviceName should be cleared", MDC.get(LoggingConstants.MDC_SERVICE_NAME));

        // Custom fields are NOT cleared by clear() - only by clearAll()
        assertEquals("customField should still exist", "custom-value", LogContext.get("customField"));
    }

    @Test
    public void shouldClearAllFieldsIncludingCustom() {
        LogContext.setUserId("user-1");
        LogContext.put("customField", "custom-value");

        LogContext.clearAll();

        assertNull("userId should be cleared", LogContext.getUserId());
        assertNull("customField should be cleared", LogContext.get("customField"));
    }
}
