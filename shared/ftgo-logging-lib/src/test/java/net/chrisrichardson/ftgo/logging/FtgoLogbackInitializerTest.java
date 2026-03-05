package net.chrisrichardson.ftgo.logging;

import net.chrisrichardson.ftgo.logging.config.FtgoLogbackInitializer;
import net.chrisrichardson.ftgo.logging.config.FtgoLoggingProperties;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Unit tests for {@link FtgoLogbackInitializer}.
 */
class FtgoLogbackInitializerTest {

    @Test
    void shouldCreateInitializerWithProperties() {
        FtgoLoggingProperties properties = new FtgoLoggingProperties();
        FtgoLogbackInitializer initializer = new FtgoLogbackInitializer(properties, "ftgo-test-service");

        assertEquals("ftgo-test-service", initializer.getServiceName());
        assertNotNull(initializer.getProperties());
    }

    @Test
    void shouldSkipInitializationWhenJsonDisabled() {
        FtgoLoggingProperties properties = new FtgoLoggingProperties();
        properties.setJsonEnabled(false);

        FtgoLogbackInitializer initializer = new FtgoLogbackInitializer(properties, "ftgo-test-service");
        // Should not throw when JSON is disabled
        initializer.initialize();

        assertEquals("ftgo-test-service", initializer.getServiceName());
    }

    @Test
    void shouldInitializeWithAsyncEnabled() {
        FtgoLoggingProperties properties = new FtgoLoggingProperties();
        properties.setJsonEnabled(true);
        properties.setAsyncEnabled(true);
        properties.setAsyncQueueSize(2048);

        FtgoLogbackInitializer initializer = new FtgoLogbackInitializer(properties, "ftgo-test-service");
        // Should not throw
        initializer.initialize();

        assertEquals("ftgo-test-service", initializer.getServiceName());
    }

    @Test
    void shouldInitializeWithSyncMode() {
        FtgoLoggingProperties properties = new FtgoLoggingProperties();
        properties.setJsonEnabled(true);
        properties.setAsyncEnabled(false);

        FtgoLogbackInitializer initializer = new FtgoLogbackInitializer(properties, "ftgo-test-service");
        // Should not throw
        initializer.initialize();

        assertEquals("ftgo-test-service", initializer.getServiceName());
    }
}
