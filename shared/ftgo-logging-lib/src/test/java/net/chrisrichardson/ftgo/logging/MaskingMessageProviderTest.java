package net.chrisrichardson.ftgo.logging;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.LoggingEvent;
import com.fasterxml.jackson.core.JsonGenerator;
import net.chrisrichardson.ftgo.logging.masking.MaskingMessageProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link MaskingMessageProvider}.
 */
class MaskingMessageProviderTest {

    private MaskingMessageProvider provider;

    @BeforeEach
    void setUp() {
        provider = new MaskingMessageProvider();
    }

    @Test
    void shouldHaveCorrectDefaultFieldName() {
        assertEquals("message", provider.getFieldName());
    }

    @Test
    void shouldMaskSensitiveDataInJsonOutput() throws IOException {
        LoggerContext context = new LoggerContext();
        Logger logger = context.getLogger("test");
        LoggingEvent event = new LoggingEvent(
                "test", logger, Level.INFO,
                "User password=secret123 logged in", null, null);

        // Capture the written JSON
        StringWriter writer = new StringWriter();
        com.fasterxml.jackson.core.JsonFactory factory = new com.fasterxml.jackson.core.JsonFactory();
        JsonGenerator generator = factory.createGenerator(writer);
        generator.writeStartObject();

        provider.writeTo(generator, event);

        generator.writeEndObject();
        generator.flush();

        String json = writer.toString();
        assertTrue(json.contains("********"));
        assertTrue(json.contains("message"));
    }

    @Test
    void shouldHandleNullMessage() throws IOException {
        LoggerContext context = new LoggerContext();
        Logger logger = context.getLogger("test");
        LoggingEvent event = new LoggingEvent(
                "test", logger, Level.INFO,
                null, null, null);

        StringWriter writer = new StringWriter();
        com.fasterxml.jackson.core.JsonFactory factory = new com.fasterxml.jackson.core.JsonFactory();
        JsonGenerator generator = factory.createGenerator(writer);
        generator.writeStartObject();

        // Should not throw
        provider.writeTo(generator, event);

        generator.writeEndObject();
        generator.flush();

        // Output should be just empty object since message is null
        String json = writer.toString();
        assertEquals("{}", json);
    }
}
