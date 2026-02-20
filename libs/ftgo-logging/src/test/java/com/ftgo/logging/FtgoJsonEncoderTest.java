package com.ftgo.logging;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.LoggingEvent;
import com.ftgo.logging.encoder.FtgoJsonEncoder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class FtgoJsonEncoderTest {

    private FtgoJsonEncoder encoder;
    private LoggerContext loggerContext;

    @BeforeEach
    void setUp() {
        loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        encoder = new FtgoJsonEncoder();
        encoder.setContext(loggerContext);
        encoder.setServiceName("test-service");
        encoder.start();
        MDC.clear();
    }

    @Test
    void encodesBasicLogEvent() {
        LoggingEvent event = createEvent(Level.INFO, "Test message");
        String json = new String(encoder.encode(event), StandardCharsets.UTF_8);

        assertThat(json).contains("\"level\":\"INFO\"");
        assertThat(json).contains("\"message\":\"Test message\"");
        assertThat(json).contains("\"service\":\"test-service\"");
        assertThat(json).contains("\"timestamp\":");
    }

    @Test
    void includesTraceIdFromMdc() {
        MDC.put("traceId", "abc123");
        MDC.put("spanId", "def456");

        LoggingEvent event = createEvent(Level.INFO, "Traced message");
        String json = new String(encoder.encode(event), StandardCharsets.UTF_8);

        assertThat(json).contains("\"traceId\":\"abc123\"");
        assertThat(json).contains("\"spanId\":\"def456\"");
    }

    @Test
    void handlesSpecialCharactersInMessage() {
        LoggingEvent event = createEvent(Level.INFO, "Message with \"quotes\" and \nnewline");
        String json = new String(encoder.encode(event), StandardCharsets.UTF_8);

        assertThat(json).contains("\\\"quotes\\\"");
        assertThat(json).contains("\\n");
    }

    private LoggingEvent createEvent(Level level, String message) {
        LoggingEvent event = new LoggingEvent();
        event.setLoggerContext(loggerContext);
        event.setLevel(level);
        event.setMessage(message);
        event.setLoggerName("com.ftgo.test.TestLogger");
        event.setThreadName("test-thread");
        event.setTimeStamp(System.currentTimeMillis());
        return event;
    }
}
