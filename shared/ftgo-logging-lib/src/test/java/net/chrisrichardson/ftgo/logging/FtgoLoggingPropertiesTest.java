package net.chrisrichardson.ftgo.logging;

import net.chrisrichardson.ftgo.logging.config.FtgoLoggingProperties;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link FtgoLoggingProperties}.
 */
class FtgoLoggingPropertiesTest {

    @Test
    void shouldHaveDefaultValues() {
        FtgoLoggingProperties properties = new FtgoLoggingProperties();

        assertTrue(properties.isEnabled());
        assertTrue(properties.isJsonEnabled());
        assertTrue(properties.isAsyncEnabled());
        assertEquals(1024, properties.getAsyncQueueSize());
        assertEquals(0, properties.getAsyncDiscardThreshold());
        assertEquals(false, properties.isIncludeCallerData());
        assertNull(properties.getServiceName());
        assertEquals("X-Correlation-ID", properties.getCorrelationIdHeader());
    }

    @Test
    void shouldAllowCustomConfiguration() {
        FtgoLoggingProperties properties = new FtgoLoggingProperties();

        properties.setEnabled(false);
        properties.setJsonEnabled(false);
        properties.setAsyncEnabled(false);
        properties.setAsyncQueueSize(2048);
        properties.setAsyncDiscardThreshold(10);
        properties.setIncludeCallerData(true);
        properties.setServiceName("ftgo-order-service");
        properties.setCorrelationIdHeader("X-Request-ID");

        assertEquals(false, properties.isEnabled());
        assertEquals(false, properties.isJsonEnabled());
        assertEquals(false, properties.isAsyncEnabled());
        assertEquals(2048, properties.getAsyncQueueSize());
        assertEquals(10, properties.getAsyncDiscardThreshold());
        assertTrue(properties.isIncludeCallerData());
        assertEquals("ftgo-order-service", properties.getServiceName());
        assertEquals("X-Request-ID", properties.getCorrelationIdHeader());
    }
}
