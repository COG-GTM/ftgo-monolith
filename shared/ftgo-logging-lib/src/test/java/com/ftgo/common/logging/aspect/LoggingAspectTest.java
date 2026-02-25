package com.ftgo.common.logging.aspect;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests for {@link LoggingAspect}.
 */
class LoggingAspectTest {

    private Logger logger;
    private ListAppender<ILoggingEvent> listAppender;

    @BeforeEach
    void setUp() {
        logger = (Logger) LoggerFactory.getLogger(LoggingAspectTest.class);
        listAppender = new ListAppender<>();
        listAppender.start();
        logger.addAppender(listAppender);
        // Ensure DEBUG is enabled for the test
        logger.setLevel(ch.qos.logback.classic.Level.DEBUG);
    }

    @AfterEach
    void tearDown() {
        logger.detachAppender(listAppender);
    }

    @Test
    @DisplayName("logEntry logs method entry at DEBUG level")
    void logEntryLogsAtDebug() {
        LoggingAspect.logEntry(LoggingAspectTest.class, "testMethod", "arg1", "arg2");

        assertThat(listAppender.list).hasSize(1);
        ILoggingEvent event = listAppender.list.get(0);
        assertThat(event.getLevel().toString()).isEqualTo("DEBUG");
        assertThat(event.getFormattedMessage()).contains("Entering");
        assertThat(event.getFormattedMessage()).contains("LoggingAspectTest");
        assertThat(event.getFormattedMessage()).contains("testMethod");
        assertThat(event.getFormattedMessage()).contains("2 argument(s)");
    }

    @Test
    @DisplayName("logExit logs method exit with result type at DEBUG level")
    void logExitLogsWithResultType() {
        LoggingAspect.logExit(LoggingAspectTest.class, "testMethod", "result");

        assertThat(listAppender.list).hasSize(1);
        ILoggingEvent event = listAppender.list.get(0);
        assertThat(event.getLevel().toString()).isEqualTo("DEBUG");
        assertThat(event.getFormattedMessage()).contains("Exiting");
        assertThat(event.getFormattedMessage()).contains("String");
    }

    @Test
    @DisplayName("logExit for void methods")
    void logExitVoid() {
        LoggingAspect.logExit(LoggingAspectTest.class, "testMethod");

        assertThat(listAppender.list).hasSize(1);
        assertThat(listAppender.list.get(0).getFormattedMessage()).contains("Exiting");
    }

    @Test
    @DisplayName("logException logs at ERROR level with exception details")
    void logExceptionLogsAtError() {
        RuntimeException exception = new RuntimeException("test error");
        LoggingAspect.logException(LoggingAspectTest.class, "testMethod", exception);

        assertThat(listAppender.list).hasSize(1);
        ILoggingEvent event = listAppender.list.get(0);
        assertThat(event.getLevel().toString()).isEqualTo("ERROR");
        assertThat(event.getFormattedMessage()).contains("Exception");
        assertThat(event.getFormattedMessage()).contains("RuntimeException");
        assertThat(event.getFormattedMessage()).contains("test error");
    }

    @Test
    @DisplayName("logExternalCall logs at INFO level")
    void logExternalCall() {
        LoggingAspect.logExternalCall(LoggingAspectTest.class, "restaurant-service", "getRestaurant");

        assertThat(listAppender.list).hasSize(1);
        ILoggingEvent event = listAppender.list.get(0);
        assertThat(event.getLevel().toString()).isEqualTo("INFO");
        assertThat(event.getFormattedMessage()).contains("restaurant-service");
        assertThat(event.getFormattedMessage()).contains("getRestaurant");
    }

    @Test
    @DisplayName("logExternalCallComplete logs with duration")
    void logExternalCallComplete() {
        LoggingAspect.logExternalCallComplete(LoggingAspectTest.class,
                "restaurant-service", "getRestaurant", 45L);

        assertThat(listAppender.list).hasSize(1);
        assertThat(listAppender.list.get(0).getFormattedMessage()).contains("45ms");
    }

    @Test
    @DisplayName("logBusinessEvent logs with event name and details")
    void logBusinessEvent() {
        LoggingAspect.logBusinessEvent(LoggingAspectTest.class,
                "ORDER_CREATED", "orderId=ORD-123");

        assertThat(listAppender.list).hasSize(1);
        ILoggingEvent event = listAppender.list.get(0);
        assertThat(event.getFormattedMessage()).contains("[BUSINESS_EVENT]");
        assertThat(event.getFormattedMessage()).contains("ORDER_CREATED");
        assertThat(event.getFormattedMessage()).contains("orderId=ORD-123");
    }

    @Test
    @DisplayName("logMethodExecution wraps successful execution")
    void logMethodExecutionSuccess() throws Throwable {
        String result = LoggingAspect.logMethodExecution(
                LoggingAspectTest.class,
                "testMethod",
                new Object[]{"arg1"},
                () -> "success"
        );

        assertThat(result).isEqualTo("success");
        assertThat(listAppender.list).hasSize(2); // entry + exit
        assertThat(listAppender.list.get(0).getFormattedMessage()).contains("Entering");
        assertThat(listAppender.list.get(1).getFormattedMessage()).contains("Exiting");
    }

    @Test
    @DisplayName("logMethodExecution wraps failed execution with error log")
    void logMethodExecutionFailure() {
        assertThatThrownBy(() ->
                LoggingAspect.logMethodExecution(
                        LoggingAspectTest.class,
                        "testMethod",
                        new Object[]{},
                        () -> { throw new RuntimeException("test failure"); }
                )
        ).isInstanceOf(RuntimeException.class);

        assertThat(listAppender.list).hasSize(2); // entry + error
        assertThat(listAppender.list.get(0).getFormattedMessage()).contains("Entering");
        assertThat(listAppender.list.get(1).getLevel().toString()).isEqualTo("ERROR");
    }
}
