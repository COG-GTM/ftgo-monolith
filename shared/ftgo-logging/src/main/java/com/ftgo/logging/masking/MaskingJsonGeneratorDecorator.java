package com.ftgo.logging.masking;

import com.fasterxml.jackson.core.JsonGenerator;
import net.logstash.logback.decorate.JsonGeneratorDecorator;

/**
 * JSON generator decorator that applies sensitive data masking to JSON-formatted log output.
 *
 * <p>This decorator wraps the Jackson {@link JsonGenerator} to intercept string values
 * written to the JSON log output and apply sensitive data masking before they are serialized.</p>
 *
 * <p>Usage in logback configuration with LogstashEncoder:</p>
 * <pre>
 * &lt;encoder class="net.logstash.logback.encoder.LogstashEncoder"&gt;
 *   &lt;jsonGeneratorDecorator class="com.ftgo.logging.masking.MaskingJsonGeneratorDecorator" /&gt;
 * &lt;/encoder&gt;
 * </pre>
 */
public class MaskingJsonGeneratorDecorator implements JsonGeneratorDecorator {

    @Override
    public JsonGenerator decorate(JsonGenerator generator) {
        return new MaskingJsonGenerator(generator);
    }

    /**
     * Delegating JsonGenerator that masks sensitive data in string values.
     */
    private static class MaskingJsonGenerator extends com.fasterxml.jackson.core.util.JsonGeneratorDelegate {

        MaskingJsonGenerator(JsonGenerator delegate) {
            super(delegate);
        }

        @Override
        public void writeString(String text) throws java.io.IOException {
            if (text != null) {
                super.writeString(SensitiveDataMaskingConverter.maskSensitiveData(text));
            } else {
                super.writeString(text);
            }
        }

        @Override
        public void writeFieldName(String name) throws java.io.IOException {
            super.writeFieldName(name);
        }
    }
}
