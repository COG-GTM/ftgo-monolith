package net.chrisrichardson.ftgo.logging;

import com.fasterxml.jackson.core.JsonGenerator;
import net.logstash.logback.composite.AbstractJsonProvider;

import ch.qos.logback.classic.spi.ILoggingEvent;

import java.io.IOException;

/**
 * Custom Logstash JSON provider that applies sensitive data masking to the
 * {@code message} field in JSON log output. This ensures that sensitive data
 * (credit cards, SSNs, passwords, tokens, emails) is masked in both
 * pattern-based and JSON-based appenders.
 *
 * <p>This provider works with {@link net.logstash.logback.encoder.LogstashEncoder}
 * by intercepting the message serialization and applying the same masking rules
 * as {@link SensitiveDataMaskingConverter}.
 *
 * <p>Configure in logback-spring.xml within a LogstashEncoder:
 * <pre>{@code
 * <encoder class="net.logstash.logback.encoder.LogstashEncoder">
 *     <provider class="net.chrisrichardson.ftgo.logging.SensitiveDataMaskingJsonProvider"/>
 *     ...
 * </encoder>
 * }</pre>
 */
public class SensitiveDataMaskingJsonProvider extends AbstractJsonProvider<ILoggingEvent> {

    /**
     * The JSON field name for the masked message. Uses "message" to override
     * the default message field written by LogstashEncoder.
     */
    private static final String FIELD_NAME = "message";

    @Override
    public void writeTo(JsonGenerator generator, ILoggingEvent event) throws IOException {
        String message = event.getFormattedMessage();
        if (message != null) {
            generator.writeStringField(FIELD_NAME, SensitiveDataMaskingConverter.maskSensitiveData(message));
        }
    }
}
