package net.chrisrichardson.ftgo.logging;

import net.chrisrichardson.ftgo.logging.aspect.LoggingAspect;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link LoggingAspect}.
 */
class LoggingAspectTest {

    @Test
    void shouldCreateAspectWithConfiguration() {
        LoggingAspect aspect = new LoggingAspect(true, false, 1000);

        assertTrue(aspect.isIncludeArgs());
        assertFalse(aspect.isIncludeResult());
        assertEquals(1000, aspect.getSlowThresholdMs());
    }

    @Test
    void shouldCreateAspectWithIncludeResult() {
        LoggingAspect aspect = new LoggingAspect(false, true, 2000);

        assertFalse(aspect.isIncludeArgs());
        assertTrue(aspect.isIncludeResult());
        assertEquals(2000, aspect.getSlowThresholdMs());
    }

    @Test
    void shouldCreateAspectWithDefaultThreshold() {
        LoggingAspect aspect = new LoggingAspect(true, true, 500);

        assertTrue(aspect.isIncludeArgs());
        assertTrue(aspect.isIncludeResult());
        assertEquals(500, aspect.getSlowThresholdMs());
    }
}
