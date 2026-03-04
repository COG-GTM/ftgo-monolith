package net.chrisrichardson.ftgo.tracing;

import net.chrisrichardson.ftgo.tracing.config.FtgoTracingProperties;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link FtgoTracingProperties}.
 */
class FtgoTracingPropertiesTest {

    @Test
    void shouldHaveDefaultValues() {
        FtgoTracingProperties properties = new FtgoTracingProperties();

        assertTrue(properties.isEnabled());
        assertEquals(1.0f, properties.getSamplingProbability());
        assertEquals("http://localhost:9411/api/v2/spans", properties.getZipkinEndpoint());
        assertEquals("B3", properties.getPropagationType());
        assertNull(properties.getServiceName());
    }

    @Test
    void shouldAllowCustomConfiguration() {
        FtgoTracingProperties properties = new FtgoTracingProperties();

        properties.setEnabled(false);
        properties.setSamplingProbability(0.1f);
        properties.setZipkinEndpoint("http://zipkin:9411/api/v2/spans");
        properties.setPropagationType("W3C");
        properties.setServiceName("ftgo-order-service");

        assertEquals(false, properties.isEnabled());
        assertEquals(0.1f, properties.getSamplingProbability());
        assertEquals("http://zipkin:9411/api/v2/spans", properties.getZipkinEndpoint());
        assertEquals("W3C", properties.getPropagationType());
        assertEquals("ftgo-order-service", properties.getServiceName());
    }
}
