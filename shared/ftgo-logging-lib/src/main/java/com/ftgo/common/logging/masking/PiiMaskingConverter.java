package com.ftgo.common.logging.masking;

import ch.qos.logback.classic.pattern.ClassicConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Custom Logback converter that masks sensitive data (PII) in log messages.
 *
 * <p>Automatically detects and masks the following patterns:</p>
 * <ul>
 *   <li>Credit card numbers (13-19 digit sequences, with or without separators)</li>
 *   <li>Passwords in key-value patterns (e.g., password=secret, "password":"secret")</li>
 *   <li>Bearer tokens (Authorization: Bearer ...)</li>
 *   <li>API keys in key-value patterns</li>
 *   <li>Social Security Numbers (SSN)</li>
 *   <li>Email addresses (partially masked)</li>
 * </ul>
 *
 * <h3>Usage in logback-spring.xml</h3>
 * <pre>{@code
 * <conversionRule conversionWord="maskedMsg"
 *     converterClass="com.ftgo.common.logging.masking.PiiMaskingConverter" />
 * <pattern>%d{yyyy-MM-dd HH:mm:ss.SSS} %5level [%thread] %logger{36} - %maskedMsg%n</pattern>
 * }</pre>
 *
 * @see ch.qos.logback.classic.pattern.ClassicConverter
 */
public class PiiMaskingConverter extends ClassicConverter {

    /** Mask placeholder for sensitive data. */
    private static final String MASK = "***MASKED***";

    /** Partial mask for email addresses. */
    private static final String EMAIL_MASK_REPLACEMENT = "$1***@$2";

    // --- Patterns ---

    /**
     * Credit card number pattern: matches 13-19 digit sequences,
     * optionally separated by spaces, hyphens, or dots.
     * Covers Visa, MasterCard, Amex, Discover, etc.
     */
    private static final Pattern CREDIT_CARD_PATTERN = Pattern.compile(
            "\\b(?:\\d[ -]?){12,18}\\d\\b"
    );

    /**
     * Password pattern: matches common password key-value pairs in logs.
     * Handles formats like: password=value, "password":"value", password: value
     */
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
            "(?i)(password|passwd|pwd|secret|credential)[\"']?\\s*[:=]\\s*[\"']?([^\"',\\s}{\\]]+)[\"']?"
    );

    /**
     * Bearer token pattern: matches Authorization header values.
     */
    private static final Pattern BEARER_TOKEN_PATTERN = Pattern.compile(
            "(?i)(Bearer\\s+)([A-Za-z0-9\\-._~+/]+=*)"
    );

    /**
     * API key pattern: matches common API key patterns in logs.
     */
    private static final Pattern API_KEY_PATTERN = Pattern.compile(
            "(?i)(api[_-]?key|apikey|api[_-]?secret|access[_-]?token|auth[_-]?token)[\"']?\\s*[:=]\\s*[\"']?([^\"',\\s}{\\]]+)[\"']?"
    );

    /**
     * SSN pattern: matches US Social Security Numbers (XXX-XX-XXXX).
     */
    private static final Pattern SSN_PATTERN = Pattern.compile(
            "\\b\\d{3}-\\d{2}-\\d{4}\\b"
    );

    /**
     * Email pattern: partially masks email addresses (keeps first char and domain).
     */
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "\\b([a-zA-Z0-9._%+-])[a-zA-Z0-9._%+-]*@([a-zA-Z0-9.-]+\\.[a-zA-Z]{2,})\\b"
    );

    @Override
    public String convert(ILoggingEvent event) {
        String message = event.getFormattedMessage();
        if (message == null || message.isEmpty()) {
            return message;
        }
        return maskSensitiveData(message);
    }

    /**
     * Applies all masking patterns to the given message.
     *
     * @param message the original log message
     * @return the message with sensitive data masked
     */
    static String maskSensitiveData(String message) {
        if (message == null || message.isEmpty()) {
            return message;
        }
        String masked = message;

        // Mask credit card numbers
        masked = CREDIT_CARD_PATTERN.matcher(masked).replaceAll(MASK);

        // Mask passwords (keep the key, mask the value)
        masked = maskKeyValuePairs(masked, PASSWORD_PATTERN);

        // Mask bearer tokens (keep "Bearer ", mask the token)
        Matcher bearerMatcher = BEARER_TOKEN_PATTERN.matcher(masked);
        masked = bearerMatcher.replaceAll("$1" + MASK);

        // Mask API keys (keep the key, mask the value)
        masked = maskKeyValuePairs(masked, API_KEY_PATTERN);

        // Mask SSNs
        masked = SSN_PATTERN.matcher(masked).replaceAll(MASK);

        // Partially mask email addresses
        masked = EMAIL_PATTERN.matcher(masked).replaceAll(EMAIL_MASK_REPLACEMENT);

        return masked;
    }

    /**
     * Masks the value portion of key-value pairs while preserving the key.
     */
    private static String maskKeyValuePairs(String message, Pattern pattern) {
        Matcher matcher = pattern.matcher(message);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String key = matcher.group(1);
            // Reconstruct with masked value
            String replacement = Matcher.quoteReplacement(key + "=" + MASK);
            matcher.appendReplacement(sb, replacement);
        }
        matcher.appendTail(sb);
        return sb.toString();
    }
}
