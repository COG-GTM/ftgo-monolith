package net.chrisrichardson.ftgo.logging.masking;

import ch.qos.logback.classic.spi.ILoggingEvent;
import com.fasterxml.jackson.core.JsonGenerator;
import net.logstash.logback.composite.AbstractFieldJsonProvider;
import net.logstash.logback.composite.JsonWritingUtils;

import java.io.IOException;

/**
 * Custom JSON provider for logstash-logback-encoder that masks sensitive data
 * in the log message field when writing JSON structured logs.
 *
 * <p>This provider replaces the default message field in JSON output with
 * a masked version, ensuring sensitive data (credit cards, passwords, tokens, etc.)
 * are never written to structured log output.
 *
 * <p>Usage in logstash-logback-encoder configuration:
 * <pre>
 * &lt;encoder class="net.logstash.logback.encoder.LogstashEncoder"&gt;
 *   &lt;provider class="net.chrisrichardson.ftgo.logging.masking.MaskingMessageProvider"/&gt;
 * &lt;/encoder&gt;
 * </pre>
 */
public class MaskingMessageProvider extends AbstractFieldJsonProvider<ILoggingEvent> {

    public static final String FIELD_MESSAGE = "message";

    public MaskingMessageProvider() {
        setFieldName(FIELD_MESSAGE);
    }

    @Override
    public void writeTo(JsonGenerator generator, ILoggingEvent event) throws IOException {
        String message = event.getFormattedMessage();
        if (message != null) {
            String masked = SensitiveDataMaskingConverter.maskSensitiveData(message);
            JsonWritingUtils.writeStringField(generator, getFieldName(), masked);
        }
    }
}
