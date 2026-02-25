package com.ftgo.common.logging.context;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link LogContext}.
 */
class LogContextTest {

    @AfterEach
    void cleanup() {
        MDC.clear();
    }

    @Test
    @DisplayName("setUserId puts userId in MDC")
    void setUserId() {
        LogContext.setUserId("USR-123");
        assertThat(MDC.get(LogContext.KEY_USER_ID)).isEqualTo("USR-123");
    }

    @Test
    @DisplayName("setRequestId puts requestId in MDC")
    void setRequestId() {
        LogContext.setRequestId("REQ-456");
        assertThat(MDC.get(LogContext.KEY_REQUEST_ID)).isEqualTo("REQ-456");
    }

    @Test
    @DisplayName("setTraceId puts traceId in MDC")
    void setTraceId() {
        LogContext.setTraceId("TRACE-789");
        assertThat(MDC.get(LogContext.KEY_TRACE_ID)).isEqualTo("TRACE-789");
    }

    @Test
    @DisplayName("setSpanId puts spanId in MDC")
    void setSpanId() {
        LogContext.setSpanId("SPAN-012");
        assertThat(MDC.get(LogContext.KEY_SPAN_ID)).isEqualTo("SPAN-012");
    }

    @Test
    @DisplayName("setServiceName puts serviceName in MDC")
    void setServiceName() {
        LogContext.setServiceName("ftgo-order-service");
        assertThat(MDC.get(LogContext.KEY_SERVICE_NAME)).isEqualTo("ftgo-order-service");
    }

    @Test
    @DisplayName("setCorrelationId puts correlationId in MDC")
    void setCorrelationId() {
        LogContext.setCorrelationId("CORR-345");
        assertThat(MDC.get(LogContext.KEY_CORRELATION_ID)).isEqualTo("CORR-345");
    }

    @Test
    @DisplayName("setOperation puts operation in MDC")
    void setOperation() {
        LogContext.setOperation("createOrder");
        assertThat(MDC.get(LogContext.KEY_OPERATION)).isEqualTo("createOrder");
    }

    @Test
    @DisplayName("null values are ignored (not put in MDC)")
    void nullValuesIgnored() {
        LogContext.setUserId(null);
        assertThat(MDC.get(LogContext.KEY_USER_ID)).isNull();
    }

    @Test
    @DisplayName("clear removes all standard FTGO MDC fields")
    void clearRemovesAllStandardFields() {
        LogContext.setUserId("USR-123");
        LogContext.setRequestId("REQ-456");
        LogContext.setTraceId("TRACE-789");
        LogContext.setSpanId("SPAN-012");
        LogContext.setServiceName("ftgo-order-service");
        LogContext.setCorrelationId("CORR-345");
        LogContext.setOperation("createOrder");

        LogContext.clear();

        assertThat(MDC.get(LogContext.KEY_USER_ID)).isNull();
        assertThat(MDC.get(LogContext.KEY_REQUEST_ID)).isNull();
        assertThat(MDC.get(LogContext.KEY_TRACE_ID)).isNull();
        assertThat(MDC.get(LogContext.KEY_SPAN_ID)).isNull();
        assertThat(MDC.get(LogContext.KEY_SERVICE_NAME)).isNull();
        assertThat(MDC.get(LogContext.KEY_CORRELATION_ID)).isNull();
        assertThat(MDC.get(LogContext.KEY_OPERATION)).isNull();
    }

    @Test
    @DisplayName("clearAll removes all MDC fields including custom ones")
    void clearAllRemovesEverything() {
        LogContext.setUserId("USR-123");
        MDC.put("customField", "customValue");

        LogContext.clearAll();

        assertThat(MDC.get(LogContext.KEY_USER_ID)).isNull();
        assertThat(MDC.get("customField")).isNull();
    }

    @Test
    @DisplayName("snapshot captures current MDC state")
    void snapshotCapturesState() {
        LogContext.setUserId("USR-123");
        LogContext.setServiceName("ftgo-order-service");

        Map<String, String> snapshot = LogContext.snapshot();

        assertThat(snapshot).containsEntry(LogContext.KEY_USER_ID, "USR-123");
        assertThat(snapshot).containsEntry(LogContext.KEY_SERVICE_NAME, "ftgo-order-service");
    }

    @Test
    @DisplayName("restore restores MDC from snapshot")
    void restoreFromSnapshot() {
        LogContext.setUserId("USR-123");
        Map<String, String> snapshot = LogContext.snapshot();

        LogContext.clearAll();
        assertThat(MDC.get(LogContext.KEY_USER_ID)).isNull();

        LogContext.restore(snapshot);
        assertThat(MDC.get(LogContext.KEY_USER_ID)).isEqualTo("USR-123");
    }

    @Test
    @DisplayName("restore with null clears MDC")
    void restoreWithNullClearsMdc() {
        LogContext.setUserId("USR-123");
        LogContext.restore(null);
        assertThat(MDC.get(LogContext.KEY_USER_ID)).isNull();
    }

    @Test
    @DisplayName("put and get work for custom fields")
    void putAndGet() {
        LogContext.put("orderId", "ORD-789");
        assertThat(LogContext.get("orderId")).isEqualTo("ORD-789");
    }

    @Test
    @DisplayName("remove removes a specific field")
    void removeField() {
        LogContext.put("orderId", "ORD-789");
        LogContext.remove("orderId");
        assertThat(LogContext.get("orderId")).isNull();
    }

    @Test
    @DisplayName("Builder pattern sets all fields")
    void builderSetsAllFields() {
        LogContext.builder()
                .userId("USR-123")
                .requestId("REQ-456")
                .traceId("TRACE-789")
                .spanId("SPAN-012")
                .serviceName("ftgo-order-service")
                .correlationId("CORR-345")
                .operation("createOrder")
                .apply();

        assertThat(MDC.get(LogContext.KEY_USER_ID)).isEqualTo("USR-123");
        assertThat(MDC.get(LogContext.KEY_REQUEST_ID)).isEqualTo("REQ-456");
        assertThat(MDC.get(LogContext.KEY_TRACE_ID)).isEqualTo("TRACE-789");
        assertThat(MDC.get(LogContext.KEY_SPAN_ID)).isEqualTo("SPAN-012");
        assertThat(MDC.get(LogContext.KEY_SERVICE_NAME)).isEqualTo("ftgo-order-service");
        assertThat(MDC.get(LogContext.KEY_CORRELATION_ID)).isEqualTo("CORR-345");
        assertThat(MDC.get(LogContext.KEY_OPERATION)).isEqualTo("createOrder");
    }

    @Test
    @DisplayName("Builder with null fields does not set them")
    void builderWithNullFields() {
        LogContext.builder()
                .userId("USR-123")
                .requestId(null)
                .apply();

        assertThat(MDC.get(LogContext.KEY_USER_ID)).isEqualTo("USR-123");
        assertThat(MDC.get(LogContext.KEY_REQUEST_ID)).isNull();
    }
}
