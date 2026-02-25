package com.ftgo.common.logging.masking;

import ch.qos.logback.classic.spi.ILoggingEvent;
import com.fasterxml.jackson.core.JsonGenerator;
import net.logstash.logback.composite.AbstractJsonProvider;

import java.io.IOException;

/**
 * JSON provider for logstash-logback-encoder that masks sensitive data in log messages.
 *
 * <p>This provider replaces the default message field with a PII-masked version,
 * ensuring that structured JSON logs do not contain sensitive data like credit card
 * numbers, passwords, tokens, or SSNs.</p>
 *
 * <h3>Usage in LogstashEncoder configuration</h3>
 * <p>This provider is automatically registered when {@code ftgo.logging.masking.enabled=true}
 * via {@link com.ftgo.common.logging.config.FtgoLoggingAutoConfiguration}.</p>
 *
 * @see PiiMaskingConverter
 */
public class PiiMaskingJsonProvider extends AbstractJsonProvider<ILoggingEvent> {

    /** JSON field name for the masked message. */
    private static final String FIELD_NAME = "message";

    @Override
    public void writeTo(JsonGenerator generator, ILoggingEvent event) throws IOException {
        String originalMessage = event.getFormattedMessage();
        if (originalMessage != null) {
            String maskedMessage = PiiMaskingConverter.maskSensitiveData(originalMessage);
            generator.writeStringField(FIELD_NAME, maskedMessage);
        }
    }
}
