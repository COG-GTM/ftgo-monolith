package net.chrisrichardson.ftgo.logging;

import net.chrisrichardson.ftgo.logging.mdc.MdcContextLifecycle;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Unit tests for {@link MdcContextLifecycle}.
 */
class MdcContextLifecycleTest {

    @AfterEach
    void tearDown() {
        MDC.clear();
    }

    @Test
    void shouldSetServiceNameOnConstruction() {
        MdcContextLifecycle lifecycle = new MdcContextLifecycle("ftgo-order-service");

        assertEquals("ftgo-order-service", MDC.get(MdcContextLifecycle.MDC_SERVICE));
        assertEquals("ftgo-order-service", lifecycle.getServiceName());
    }

    @Test
    void shouldSetCorrelationId() {
        MdcContextLifecycle lifecycle = new MdcContextLifecycle("ftgo-order-service");
        lifecycle.setCorrelationId("corr-123");

        assertEquals("corr-123", MDC.get(MdcContextLifecycle.MDC_CORRELATION_ID));
    }

    @Test
    void shouldSetUserId() {
        MdcContextLifecycle lifecycle = new MdcContextLifecycle("ftgo-order-service");
        lifecycle.setUserId("user-456");

        assertEquals("user-456", MDC.get(MdcContextLifecycle.MDC_USER_ID));
    }

    @Test
    void shouldSetRequestId() {
        MdcContextLifecycle lifecycle = new MdcContextLifecycle("ftgo-order-service");
        lifecycle.setRequestId("req-789");

        assertEquals("req-789", MDC.get(MdcContextLifecycle.MDC_REQUEST_ID));
    }

    @Test
    void shouldNotSetNullValues() {
        MdcContextLifecycle lifecycle = new MdcContextLifecycle("ftgo-order-service");
        lifecycle.setCorrelationId(null);
        lifecycle.setUserId(null);
        lifecycle.setRequestId(null);

        assertNull(MDC.get(MdcContextLifecycle.MDC_CORRELATION_ID));
        assertNull(MDC.get(MdcContextLifecycle.MDC_USER_ID));
        assertNull(MDC.get(MdcContextLifecycle.MDC_REQUEST_ID));
    }

    @Test
    void shouldClearRequestContext() {
        MdcContextLifecycle lifecycle = new MdcContextLifecycle("ftgo-order-service");
        lifecycle.setCorrelationId("corr-123");
        lifecycle.setUserId("user-456");
        lifecycle.setRequestId("req-789");

        lifecycle.clearRequestContext();

        // Service name should remain
        assertEquals("ftgo-order-service", MDC.get(MdcContextLifecycle.MDC_SERVICE));
        // Request context should be cleared
        assertNull(MDC.get(MdcContextLifecycle.MDC_CORRELATION_ID));
        assertNull(MDC.get(MdcContextLifecycle.MDC_USER_ID));
        assertNull(MDC.get(MdcContextLifecycle.MDC_REQUEST_ID));
    }

    @Test
    void shouldClearAllMdc() {
        MdcContextLifecycle lifecycle = new MdcContextLifecycle("ftgo-order-service");
        lifecycle.setCorrelationId("corr-123");

        lifecycle.clearAll();

        assertNull(MDC.get(MdcContextLifecycle.MDC_SERVICE));
        assertNull(MDC.get(MdcContextLifecycle.MDC_CORRELATION_ID));
    }
}
