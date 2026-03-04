package net.chrisrichardson.ftgo.logging;

import net.chrisrichardson.ftgo.logging.context.LogContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Unit tests for {@link LogContext}.
 */
class LogContextTest {

    @AfterEach
    void tearDown() {
        MDC.clear();
    }

    @Test
    void shouldSetServiceName() {
        LogContext.setServiceName("ftgo-order-service");
        assertEquals("ftgo-order-service", MDC.get(LogContext.KEY_SERVICE));
    }

    @Test
    void shouldSetUserId() {
        LogContext.setUserId("user-123");
        assertEquals("user-123", MDC.get(LogContext.KEY_USER_ID));
    }

    @Test
    void shouldSetRequestId() {
        LogContext.setRequestId("req-456");
        assertEquals("req-456", MDC.get(LogContext.KEY_REQUEST_ID));
    }

    @Test
    void shouldSetTraceId() {
        LogContext.setTraceId("trace-789");
        assertEquals("trace-789", MDC.get(LogContext.KEY_TRACE_ID));
    }

    @Test
    void shouldSetSpanId() {
        LogContext.setSpanId("span-012");
        assertEquals("span-012", MDC.get(LogContext.KEY_SPAN_ID));
    }

    @Test
    void shouldSetCorrelationId() {
        LogContext.setCorrelationId("corr-345");
        assertEquals("corr-345", MDC.get(LogContext.KEY_CORRELATION_ID));
    }

    @Test
    void shouldNotSetNullValues() {
        LogContext.setUserId(null);
        LogContext.setRequestId(null);
        LogContext.setCorrelationId(null);

        assertNull(MDC.get(LogContext.KEY_USER_ID));
        assertNull(MDC.get(LogContext.KEY_REQUEST_ID));
        assertNull(MDC.get(LogContext.KEY_CORRELATION_ID));
    }

    @Test
    void shouldSetRequestContext() {
        LogContext.setRequestContext("user-123", "req-456", "corr-789");

        assertEquals("user-123", MDC.get(LogContext.KEY_USER_ID));
        assertEquals("req-456", MDC.get(LogContext.KEY_REQUEST_ID));
        assertEquals("corr-789", MDC.get(LogContext.KEY_CORRELATION_ID));
    }

    @Test
    void shouldClearRequestContext() {
        LogContext.setServiceName("ftgo-order-service");
        LogContext.setUserId("user-123");
        LogContext.setRequestId("req-456");
        LogContext.setCorrelationId("corr-789");

        LogContext.clearRequestContext();

        // Service name should remain
        assertEquals("ftgo-order-service", MDC.get(LogContext.KEY_SERVICE));
        // Request context should be cleared
        assertNull(MDC.get(LogContext.KEY_USER_ID));
        assertNull(MDC.get(LogContext.KEY_REQUEST_ID));
        assertNull(MDC.get(LogContext.KEY_CORRELATION_ID));
    }

    @Test
    void shouldClearAll() {
        LogContext.setServiceName("ftgo-order-service");
        LogContext.setUserId("user-123");

        LogContext.clearAll();

        assertNull(MDC.get(LogContext.KEY_SERVICE));
        assertNull(MDC.get(LogContext.KEY_USER_ID));
    }

    @Test
    void shouldGetMdcValue() {
        MDC.put("testKey", "testValue");
        assertEquals("testValue", LogContext.get("testKey"));
    }

    @Test
    void shouldExecuteWithContextAndClear() {
        LogContext.setServiceName("ftgo-order-service");
        final String[] capturedUserId = new String[1];
        final String[] capturedRequestId = new String[1];

        LogContext.withContext(() -> {
            capturedUserId[0] = MDC.get(LogContext.KEY_USER_ID);
            capturedRequestId[0] = MDC.get(LogContext.KEY_REQUEST_ID);
        }, "user-123", "req-456");

        // MDC was populated during execution
        assertEquals("user-123", capturedUserId[0]);
        assertEquals("req-456", capturedRequestId[0]);

        // Request context should be cleared after
        assertNull(MDC.get(LogContext.KEY_USER_ID));
        assertNull(MDC.get(LogContext.KEY_REQUEST_ID));

        // Service name should still be set
        assertEquals("ftgo-order-service", MDC.get(LogContext.KEY_SERVICE));
    }

    @Test
    void shouldClearContextEvenOnException() {
        LogContext.setUserId("user-123");
        LogContext.setRequestId("req-456");

        try {
            LogContext.withContext(() -> {
                throw new RuntimeException("Test exception");
            }, "user-999", "req-999");
        } catch (RuntimeException ignored) {
            // Expected
        }

        // Context should be cleared even after exception
        assertNull(MDC.get(LogContext.KEY_USER_ID));
        assertNull(MDC.get(LogContext.KEY_REQUEST_ID));
    }
}
