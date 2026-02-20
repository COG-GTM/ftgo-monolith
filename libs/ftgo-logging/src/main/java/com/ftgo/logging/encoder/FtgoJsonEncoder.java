package com.ftgo.logging.encoder;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.ThrowableProxyUtil;
import ch.qos.logback.core.encoder.EncoderBase;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Map;

public class FtgoJsonEncoder extends EncoderBase<ILoggingEvent> {

    private static final DateTimeFormatter TIMESTAMP_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX").withZone(ZoneOffset.UTC);

    private String serviceName;
    private boolean prettyPrint;

    @Override
    public byte[] headerBytes() {
        return null;
    }

    @Override
    public byte[] encode(ILoggingEvent event) {
        StringBuilder sb = new StringBuilder(512);
        sb.append('{');

        appendField(sb, "timestamp", TIMESTAMP_FORMATTER.format(Instant.ofEpochMilli(event.getTimeStamp())), true);
        appendField(sb, "level", event.getLevel().toString(), false);
        appendField(sb, "logger", event.getLoggerName(), false);
        appendField(sb, "thread", event.getThreadName(), false);
        appendField(sb, "message", escapeJson(event.getFormattedMessage()), false);

        if (serviceName != null && !serviceName.isEmpty()) {
            appendField(sb, "service", serviceName, false);
        }

        Map<String, String> mdc = event.getMDCPropertyMap();
        if (mdc != null && !mdc.isEmpty()) {
            String traceId = mdc.get("traceId");
            if (traceId != null && !traceId.isEmpty()) {
                appendField(sb, "traceId", traceId, false);
            }

            String spanId = mdc.get("spanId");
            if (spanId != null && !spanId.isEmpty()) {
                appendField(sb, "spanId", spanId, false);
            }

            String requestId = mdc.get("requestId");
            if (requestId != null && !requestId.isEmpty()) {
                appendField(sb, "requestId", requestId, false);
            }

            String userId = mdc.get("userId");
            if (userId != null && !userId.isEmpty()) {
                appendField(sb, "userId", userId, false);
            }

            if (!mdc.isEmpty()) {
                sb.append(",\"context\":{");
                boolean first = true;
                for (Map.Entry<String, String> entry : mdc.entrySet()) {
                    String key = entry.getKey();
                    if ("traceId".equals(key) || "spanId".equals(key)
                            || "requestId".equals(key) || "userId".equals(key)) {
                        continue;
                    }
                    if (!first) {
                        sb.append(',');
                    }
                    sb.append('"').append(escapeJson(key)).append("\":\"")
                            .append(escapeJson(entry.getValue())).append('"');
                    first = false;
                }
                sb.append('}');
            }
        }

        IThrowableProxy throwable = event.getThrowableProxy();
        if (throwable != null) {
            appendField(sb, "exception", throwable.getClassName() + ": " + throwable.getMessage(), false);
            appendField(sb, "stackTrace",
                    escapeJson(ThrowableProxyUtil.asString(throwable)), false);
        }

        sb.append('}');
        sb.append('\n');

        if (prettyPrint) {
            return formatPretty(sb.toString()).getBytes(StandardCharsets.UTF_8);
        }

        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public byte[] footerBytes() {
        return null;
    }

    private void appendField(StringBuilder sb, String key, String value, boolean first) {
        if (!first) {
            sb.append(',');
        }
        sb.append('"').append(key).append("\":\"").append(value).append('"');
    }

    private String escapeJson(String value) {
        if (value == null) {
            return "";
        }
        StringBuilder escaped = new StringBuilder(value.length());
        for (int i = 0; i < value.length(); i++) {
            char ch = value.charAt(i);
            switch (ch) {
                case '"':
                    escaped.append("\\\"");
                    break;
                case '\\':
                    escaped.append("\\\\");
                    break;
                case '\n':
                    escaped.append("\\n");
                    break;
                case '\r':
                    escaped.append("\\r");
                    break;
                case '\t':
                    escaped.append("\\t");
                    break;
                default:
                    if (ch < 0x20) {
                        escaped.append(String.format("\\u%04x", (int) ch));
                    } else {
                        escaped.append(ch);
                    }
            }
        }
        return escaped.toString();
    }

    private String formatPretty(String json) {
        StringBuilder pretty = new StringBuilder();
        int indent = 0;
        boolean inString = false;
        for (int i = 0; i < json.length(); i++) {
            char ch = json.charAt(i);
            if (ch == '"' && (i == 0 || json.charAt(i - 1) != '\\')) {
                inString = !inString;
            }
            if (!inString) {
                if (ch == '{' || ch == '[') {
                    pretty.append(ch).append('\n');
                    indent++;
                    pretty.append("  ".repeat(indent));
                    continue;
                }
                if (ch == '}' || ch == ']') {
                    pretty.append('\n');
                    indent--;
                    pretty.append("  ".repeat(indent)).append(ch);
                    continue;
                }
                if (ch == ',') {
                    pretty.append(ch).append('\n').append("  ".repeat(indent));
                    continue;
                }
            }
            pretty.append(ch);
        }
        return pretty.toString();
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public void setPrettyPrint(boolean prettyPrint) {
        this.prettyPrint = prettyPrint;
    }
}
